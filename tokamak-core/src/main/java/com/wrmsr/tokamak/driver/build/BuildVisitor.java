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
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Schema;
import com.wrmsr.tokamak.codec.ByteArrayInput;
import com.wrmsr.tokamak.codec.row.RowCodec;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.func.Function;
import com.wrmsr.tokamak.func.RowFunction;
import com.wrmsr.tokamak.func.RowViewFunction;
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public class BuildVisitor
        extends NodeVisitor<Collection<DriverRow>, Key>
{
    /*
    TODO:
      - node priorities
      - EJ cross lookup (one left one right)
    */
    private final DriverContextImpl context;

    public BuildVisitor(DriverContextImpl context)
    {
        this.context = checkNotNull(context);
    }

    @Override
    public Collection<DriverRow> visitCrossJoinNode(CrossJoinNode node, Key key)
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
            // List<Key> idKeys = CompositeRowCodec.split(((IdKey) key).getId().getValue()).stream()
            //         .map(Id::of)
            //         .map(Key::of)
            //         .collect(toImmutableList());
            // checkState(idKeys.size() == node.getSources().size());
            // sourceKeyPairs = Streams.zip(node.getSources().stream(), idKeys.stream(), Pair::immutable)
            //         .collect(toImmutableList());
        }
        else {
            throw new IllegalArgumentException(key.toString());
        }

        throw new IllegalStateException();
    }

    @Override
    public Collection<DriverRow> visitEquijoinNode(EquijoinNode node, Key key)
    {
        return new EquijoinBuilder(node).build(context, key);
    }

    @Override
    public Collection<DriverRow> visitFilterNode(FilterNode node, Key key)
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
    public Collection<DriverRow> visitListAggregateNode(ListAggregateNode node, Key key)
    {
        RowCodec idCodec = context.getDriver().getCodecManager().getRowIdCodec(node);
        Key childKey;
        if (key instanceof IdKey) {
            byte[] buf = ((IdKey) key).getId().getValue();
            childKey = Key.of(node.getGroupField(), idCodec.decodeSingle(node.getGroupField(), new ByteArrayInput(buf)));
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

        Collection<DriverRow> rows = context.build(node.getSource(), key);
        if (rows.size() == 1 && checkSingle(rows).isNull()) {

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
    public Collection<DriverRow> visitLookupJoinNode(LookupJoinNode node, Key key)
    {
        return super.visitLookupJoinNode(node, key);
    }

    @Override
    public Collection<DriverRow> visitPersistNode(PersistNode node, Key key)
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
    public Collection<DriverRow> visitProjectNode(ProjectNode node, Key key)
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
    public Collection<DriverRow> visitScanNode(ScanNode node, Key key)
    {
        Scanner scanner = context.getDriver().getScanner(node);
        Schema schema = context.getDriver().getCatalog().getSchemasByName().get(node.getSchemaTable().getSchema());
        Connection connection = context.getConnection(schema.getConnector());

        RowCodec idCodec = context.getDriver().getCodecManager().getRowIdCodec(node);

        Key scanKey;
        if (key instanceof IdKey) {
            byte[] buf = ((IdKey) key).getId().getValue();
            Map<String, Object> keyFields = idCodec.decodeMap(new ByteArrayInput(buf));
            scanKey = Key.of(keyFields);
        }
        else {
            scanKey = key;
        }

        List<Map<String, Object>> scanRows = scanner.scan(connection, scanKey);
        checkState(!scanRows.isEmpty());

        ImmutableList.Builder<DriverRow> rows = ImmutableList.builder();
        for (Map<String, Object> scanRow : scanRows) {
            Id id = Id.of(idCodec.encodeBytes(scanRow));
            Object[] attributes = scanRow.values().toArray();
            rows.add(
                    new DriverRow(
                            node,
                            context.getDriver().getLineagePolicy().build(),
                            id,
                            attributes));
        }

        return rows.build();
    }

    @Override
    public Collection<DriverRow> visitUnionNode(UnionNode node, Key key)
    {
        return super.visitUnionNode(node, key);
    }

    @Override
    public Collection<DriverRow> visitUnnestNode(UnnestNode node, Key key)
    {
        return super.visitUnnestNode(node, key);
    }

    @Override
    public Collection<DriverRow> visitValuesNode(ValuesNode node, Key key)
    {
        return super.visitValuesNode(node, key);
    }
}
