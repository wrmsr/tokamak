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
import com.wrmsr.tokamak.node.CrossJoinNode;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.ListAggregateNode;
import com.wrmsr.tokamak.node.LookupJoinNode;
import com.wrmsr.tokamak.node.PersistNode;
import com.wrmsr.tokamak.node.ProjectNode;
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
        extends NodeVisitor<List<BuildOutput>, Key>
{
    private final DriverContextImpl context;

    public BuildNodeVisitor(DriverContextImpl context)
    {
        this.context = checkNotNull(context);
    }

    @Override
    public List<BuildOutput> visitCrossJoinNode(CrossJoinNode node, Key key)
    {
        return super.visitCrossJoinNode(node, key);
    }

    @Override
    public List<BuildOutput> visitEquijoinNode(EquijoinNode node, Key key)
    {
        return super.visitEquijoinNode(node, key);
    }

    @Override
    public List<BuildOutput> visitFilterNode(FilterNode node, Key key)
    {
        ImmutableList.Builder<BuildOutput> ret = ImmutableList.builder();
        for (DriverRow row : context.build(node.getSource(), key)) {
            Object[] attributes;
            if (node.getPredicate().test(row)) {
                attributes = row.getAttributes();
            }
            else {
                attributes = null;
            }
            ret.add(
                    new BuildOutput(
                            new DriverRow(
                                    node,
                                    context.getDriver().getLineagePolicy().build(row),
                                    row.getId(),
                                    attributes)));
        }
        return ret.build();
    }

    @Override
    public List<BuildOutput> visitListAggregateNode(ListAggregateNode node, Key key)
    {
        return super.visitListAggregateNode(node, key);
    }

    @Override
    public List<BuildOutput> visitLookupJoinNode(LookupJoinNode node, Key key)
    {
        return super.visitLookupJoinNode(node, key);
    }

    @Override
    public List<BuildOutput> visitPersistNode(PersistNode node, Key key)
    {
        return super.visitPersistNode(node, key);
    }

    @Override
    public List<BuildOutput> visitProjectNode(ProjectNode node, Key key)
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

        // ImmutableList.Builder<BuildOutput> ret = ImmutableList.builder();
        // for (DriverRow row : context.build(node.getSource(), sourceKey)) {
        //     Object[] attributes = new Object[node.getFields().size()];
        //     for ()
        //
        // }
        // return ret.build();
        throw new IllegalStateException();
    }

    @Override
    public List<BuildOutput> visitScanNode(ScanNode node, Key key)
    {
        Scanner scanner = context.getDriver().getScanner(node);
        Schema schema = context.getDriver().getCatalog().getSchemasByName().get(node.getSchemaTable().getSchema());
        Connection connection = context.getConnection(schema.getConnector());
        List<Row> rows = scanner.scan(connection, key);
        checkState(!rows.isEmpty());
        return rows.stream()
                .map(r -> new BuildOutput(
                        new DriverRow(
                                node,
                                context.getDriver().getLineagePolicy().build(),
                                r.getId(),
                                r.getAttributes())))
                .collect(toImmutableList());
    }

    @Override
    public List<BuildOutput> visitUnionNode(UnionNode node, Key key)
    {
        return super.visitUnionNode(node, key);
    }

    @Override
    public List<BuildOutput> visitUnnestNode(UnnestNode node, Key key)
    {
        return super.visitUnnestNode(node, key);
    }

    @Override
    public List<BuildOutput> visitValuesNode(ValuesNode node, Key key)
    {
        return super.visitValuesNode(node, key);
    }
}
