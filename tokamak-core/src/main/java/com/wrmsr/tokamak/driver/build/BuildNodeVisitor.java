/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wrmsr.tokamak.driver.build;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Schema;
import com.wrmsr.tokamak.codec.CompositeRowIdCodec;
import com.wrmsr.tokamak.codec.IdCodecs;
import com.wrmsr.tokamak.codec.RowIdCodec;
import com.wrmsr.tokamak.codec.ScalarRowIdCodec;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.function.Function;
import com.wrmsr.tokamak.function.RowFunction;
import com.wrmsr.tokamak.function.RowViewFunction;
import com.wrmsr.tokamak.layout.RowView;
import com.wrmsr.tokamak.node.CrossJoinNode;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.ListAggregateNode;
import com.wrmsr.tokamak.node.LookupJoinNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.PersistNode;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.Projection;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.UnionNode;
import com.wrmsr.tokamak.node.UnnestNode;
import com.wrmsr.tokamak.node.ValuesNode;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public class BuildNodeVisitor
        extends NodeVisitor<List<DriverRow>, Key>
{
    /*
    TODO:
      - node priorities
      - EJ cross lookup (one left one right)
    */
    private final DriverContextImpl context;

    public BuildNodeVisitor(DriverContextImpl context)
    {
        this.context = checkNotNull(context);
    }

    @Override
    public List<DriverRow> visitCrossJoinNode(CrossJoinNode node, Key key)
    {
        List<Pair<Node, Key>> sourceKeyPairs;
        if (key instanceof AllKey) {
            sourceKeyPairs = node.getSources().stream()
                    .map(s -> Pair.immutable(s, key))
                    .collect(toImmutableList());
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Node lookupSource = fieldKey.getValuesByField().keySet().stream()
                    .map(f -> checkNotNull(node.getSourcesByField().get(f)))
                    .collect(toSingle());
            sourceKeyPairs = ImmutableList.<Pair<Node, Key>>builder()
                    .add(Pair.immutable(lookupSource, key))
                    .addAll(node.getSources().stream()
                            .filter(s -> s != lookupSource)
                            .map(s -> Pair.<Node, Key>immutable(s, Key.all()))
                            .collect(toImmutableList()))
                    .build();
        }
        else if (key instanceof IdKey) {
            List<Key> idKeys = CompositeRowIdCodec.split(((IdKey) key).getId().getValue()).stream()
                    .map(Id::of)
                    .map(Key::of)
                    .collect(toImmutableList());
            checkState(idKeys.size() == node.getSources().size());
            sourceKeyPairs = Streams.zip(node.getSources().stream(), idKeys.stream(), Pair::immutable)
                    .collect(toImmutableList());
        }
        else {
            throw new IllegalArgumentException(key.toString());
        }

        throw new IllegalStateException();
    }

    @Override
    public List<DriverRow> visitEquijoinNode(EquijoinNode node, Key key)
    {
        Pair<EquijoinNode.Branch, FieldKey> branchFieldKeyPair;

        if (key instanceof IdKey) {
            throw new IllegalStateException();
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Set<EquijoinNode.Branch> idBranches = node.getBranchSetsByKeyFieldSet().get(fieldKey.getValuesByField().keySet());
            if (idBranches == null) {
                EquijoinNode.Branch idBranch = checkNotNull(idBranches.iterator().next());
                // branchFieldKeyPair = Pair.immutable(idBranch, Key.of())
                throw new IllegalStateException();
            }
            else {
                Set<EquijoinNode.Branch> lookupBranches = fieldKey.getValuesByField().keySet().stream()
                        .map(f -> checkNotNull(node.getBranchesByUniqueField().get(f)))
                        .collect(toImmutableSet());
                if (lookupBranches.size() != 1) {
                    throw new IllegalStateException("Multiple lookup branches: " + lookupBranches);
                }
                EquijoinNode.Branch lookupBranch = checkSingle(lookupBranches);
                branchFieldKeyPair = Pair.immutable(lookupBranch, fieldKey);
            }
        }
        else {
            throw new IllegalStateException();
        }

        List<DriverRow> lookupRows = context.build(branchFieldKeyPair.first().getNode(), branchFieldKeyPair.second());
        ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
        for (DriverRow lookupRow : lookupRows) {
            List<Object> keyValues = branchFieldKeyPair.first().getFields().stream()
                    .map(lookupRow.getRowView()::get)
                    .collect(toImmutableList());

            ImmutableList.Builder<List<DriverRow>> innerRowLists = ImmutableList.builder();
            innerRowLists.add(lookupRows);
            for (EquijoinNode.Branch nonLookupBranch : node.getBranches()) {
                if (nonLookupBranch == branchFieldKeyPair.first()) {
                    continue;
                }
                FieldKey nonLookupKey = Key.of(
                        IntStream.range(0, node.getKeyLength())
                                .boxed()
                                .collect(toImmutableMap(
                                        i -> nonLookupBranch.getFields().get(i),
                                        keyValues::get)));
                innerRowLists.add(context.build(nonLookupBranch.getNode(), nonLookupKey));
            }

            for (List<DriverRow> product : Lists.cartesianProduct(innerRowLists.build())) {
                Object[] attributes = new Object[node.getRowLayout().getFields().size()];
                for (DriverRow row : product) {
                    for (Map.Entry<String, Object> e : row.getRowView()) {
                        int pos = node.getRowLayout().getPositionsByField().get(e.getKey());
                        attributes[pos] = e.getValue();
                    }
                }
                Id id = Id.of(
                        CompositeRowIdCodec.join(
                                product.stream()
                                        .map(DriverRow::getId)
                                        .map(Id::getValue)
                                        .collect(toImmutableList())));
                ret.add(
                        new DriverRow(
                                node,
                                context.getDriver().getLineagePolicy().build(product),
                                id,
                                attributes));
            }
        }

        return ret.build();
    }

    @Override
    public List<DriverRow> visitFilterNode(FilterNode node, Key key)
    {
        ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
        for (DriverRow row : context.build(node.getSource(), key)) {
            Object[] attributes;
            if (node.getPredicate().test(row)) {
                attributes = row.getAttributes();
            }
            else {
                attributes = null;
            }
            ret.add(
                    new DriverRow(
                            node,
                            context.getDriver().getLineagePolicy().build(row),
                            row.getId(),
                            attributes));
        }
        return ret.build();
    }

    @Override
    public List<DriverRow> visitListAggregateNode(ListAggregateNode node, Key key)
    {
        RowIdCodec idCodec = new ScalarRowIdCodec<>(
                node.getGroupField(), IdCodecs.CODECS_BY_TYPE.get(node.getFields().get(node.getGroupField())));
        Key childKey;
        if (key instanceof IdKey) {
            childKey = Key.of(node.getGroupField(), idCodec.decode(((IdKey) key).getId().getValue()));
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Map.Entry<String, Object> fieldKeyEntry = checkSingle(fieldKey);
            checkArgument(fieldKeyEntry.getKey().equals(node.getGroupField()));
            childKey = Key.of(node.getGroupField(), fieldKeyEntry.getValue());
        }
        else if (key instanceof AllKey) {
            childKey = key;
        }
        else {
            throw new IllegalArgumentException(key.toString());
        }

        List<DriverRow> rows = context.build(node.getSource(), key);
        if (rows.size() == 1 && rows.get(0).isNull()) {

        }

        Map<Object, List<DriverRow>> groups = new LinkedHashMap<>();
        for (DriverRow row : rows) {
            if (row.isNull()) {

            }
            // Object group =
            //         groups.computeIfAbsent(ro)
        }

        throw new IllegalStateException();
    }

    @Override
    public List<DriverRow> visitLookupJoinNode(LookupJoinNode node, Key key)
    {
        return super.visitLookupJoinNode(node, key);
    }

    @Override
    public List<DriverRow> visitPersistNode(PersistNode node, Key key)
    {
        return context.build(node.getSource(), key).stream()
                .map(row -> new DriverRow(
                        node,
                        context.getDriver().getLineagePolicy().build(row),
                        row.getId(),
                        row.getAttributes()))
                .collect(toImmutableList());
    }

    @Override
    public List<DriverRow> visitProjectNode(ProjectNode node, Key key)
    {
        Key sourceKey;
        if (key instanceof IdKey || key instanceof AllKey) {
            sourceKey = key;
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            sourceKey = Key.of(
                    fieldKey.stream()
                            .collect(toImmutableMap(
                                    e -> node.getProjection().getInputFieldsByOutput().get(e.getKey()),
                                    Map.Entry::getValue)));
        }
        else {
            throw new IllegalArgumentException(key.toString());
        }

        ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
        for (DriverRow row : context.build(node.getSource(), sourceKey)) {
            RowView rowView = row.getRowView();
            Object[] attributes = new Object[node.getFields().size()];
            int pos = 0;
            for (Map.Entry<String, Projection.Input> entry : node.getProjection()) {
                Object value;

                if (entry.getValue() instanceof Projection.FieldInput) {
                    Projection.FieldInput fieldInput = (Projection.FieldInput) entry.getValue();
                    value = rowView.get(fieldInput.getField());
                }
                else if (entry.getValue() instanceof Projection.FunctionInput) {
                    Projection.FunctionInput functionInput = (Projection.FunctionInput) entry.getValue();
                    Function function = context.getDriver().getCatalog().getFunctionsByName().get(functionInput.getFunction());
                    checkState(function.getType().equals(functionInput.getType()));
                    if (function instanceof RowFunction) {
                        value = ((RowFunction) function).invoke(row);
                    }
                    else if (function instanceof RowViewFunction) {
                        value = ((RowViewFunction) function).invoke(rowView);
                    }
                    else {
                        throw new IllegalStateException(function.toString());
                    }
                }
                else {
                    throw new IllegalStateException(entry.toString());
                }

                attributes[pos++] = value;
            }

            ret.add(
                    new DriverRow(
                            node,
                            context.getDriver().getLineagePolicy().build(row),
                            row.getId(),
                            attributes));
        }

        return ret.build();
    }

    @Override
    public List<DriverRow> visitScanNode(ScanNode node, Key key)
    {
        Scanner scanner = context.getDriver().getScanner(node);
        Schema schema = context.getDriver().getCatalog().getSchemasByName().get(node.getSchemaTable().getSchema());
        Connection connection = context.getConnection(schema.getConnector());
        List<Row> rows = scanner.scan(connection, key);
        checkState(!rows.isEmpty());
        return rows.stream()
                .map(r -> new DriverRow(
                        node,
                        context.getDriver().getLineagePolicy().build(),
                        r.getId(),
                        r.getAttributes()))
                .collect(toImmutableList());
    }

    @Override
    public List<DriverRow> visitUnionNode(UnionNode node, Key key)
    {
        return super.visitUnionNode(node, key);
    }

    @Override
    public List<DriverRow> visitUnnestNode(UnnestNode node, Key key)
    {
        return super.visitUnnestNode(node, key);
    }

    @Override
    public List<DriverRow> visitValuesNode(ValuesNode node, Key key)
    {
        return super.visitValuesNode(node, key);
    }
}
