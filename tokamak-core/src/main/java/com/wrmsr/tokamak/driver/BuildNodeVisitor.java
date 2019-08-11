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
package com.wrmsr.tokamak.driver;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Schema;
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
import com.wrmsr.tokamak.node.PersistNode;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.Projection;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.UnionNode;
import com.wrmsr.tokamak.node.UnnestNode;
import com.wrmsr.tokamak.node.ValuesNode;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class BuildNodeVisitor
        extends NodeVisitor<List<DriverRow>, Key>
{
    private final DriverContextImpl context;

    public BuildNodeVisitor(DriverContextImpl context)
    {
        this.context = checkNotNull(context);
    }

    @Override
    public List<DriverRow> visitCrossJoinNode(CrossJoinNode node, Key key)
    {
        return super.visitCrossJoinNode(node, key);
    }

    @Override
    public List<DriverRow> visitEquijoinNode(EquijoinNode node, Key key)
    {
        if (key instanceof IdKey) {
            throw new IllegalStateException();
        }
        else if (key instanceof FieldKey) {
        }

        throw new IllegalStateException();
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
        return super.visitListAggregateNode(node, key);
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
