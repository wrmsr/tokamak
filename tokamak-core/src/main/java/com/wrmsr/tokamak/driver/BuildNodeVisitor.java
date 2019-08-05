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
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.SimpleRow;
import com.wrmsr.tokamak.node.CrossJoinNode;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.ListAggregateNode;
import com.wrmsr.tokamak.node.LookupJoinNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.PersistNode;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.node.UnionNode;
import com.wrmsr.tokamak.node.UnnestNode;
import com.wrmsr.tokamak.node.ValuesNode;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class BuildNodeVisitor
        extends NodeVisitor<List<BuildOutput>, BuildContext>
{
    @Override
    protected List<BuildOutput> visitNode(Node node, BuildContext context)
    {
        throw new IllegalStateException();
    }

    @Override
    public List<BuildOutput> visitCrossJoinNode(CrossJoinNode node, BuildContext context)
    {
        return super.visitCrossJoinNode(node, context);
    }

    @Override
    public List<BuildOutput> visitEquijoinNode(EquijoinNode node, BuildContext context)
    {
        return super.visitEquijoinNode(node, context);
    }

    @Override
    public List<BuildOutput> visitFilterNode(FilterNode node, BuildContext context)
    {
        ImmutableList.Builder<BuildOutput> ret = ImmutableList.builder();
        for (DriverRow row : context.getDriverContext().build(node.getSource(), context.getKey())) {
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
                                    context.getDriverContext().getDriver().getLineagePolicy().build(row),
                                    row.getId(),
                                    attributes)));
        }
        return ret.build();
    }

    @Override
    public List<BuildOutput> visitListAggregateNode(ListAggregateNode node, BuildContext context)
    {
        return super.visitListAggregateNode(node, context);
    }

    @Override
    public List<BuildOutput> visitLookupJoinNode(LookupJoinNode node, BuildContext context)
    {
        return super.visitLookupJoinNode(node, context);
    }

    @Override
    public List<BuildOutput> visitPersistNode(PersistNode node, BuildContext context)
    {
        return super.visitPersistNode(node, context);
    }

    @Override
    public List<BuildOutput> visitProjectNode(ProjectNode node, BuildContext context)
    {
        return super.visitProjectNode(node, context);
    }

    @Override
    public List<BuildOutput> visitScanNode(ScanNode node, BuildContext context)
    {
        Scanner scanner = new Scanner(
                node.getTable(),
                context.getDriverContext().getDriver().getTableLayout(node.getTable()),
                node.getFields().keySet());

        if (context.getKey() instanceof FieldKey) {
            List<SimpleRow> rows = scanner.scan(context.getDriverContext().getJdbiHandle(), context.getKey());
            return rows.stream()
                    .map(r -> new BuildOutput(
                            new DriverRow(
                                    node,
                                    context.getDriverContext().getDriver().getLineagePolicy().build(),
                                    r.getId(),
                                    r.getAttributes())))
                    .collect(toImmutableList());
        }
        else {
            throw new IllegalArgumentException(context.getKey().toString());
        }
    }

    @Override
    public List<BuildOutput> visitUnionNode(UnionNode node, BuildContext context)
    {
        return super.visitUnionNode(node, context);
    }

    @Override
    public List<BuildOutput> visitUnnestNode(UnnestNode node, BuildContext context)
    {
        return super.visitUnnestNode(node, context);
    }

    @Override
    public List<BuildOutput> visitValuesNode(ValuesNode node, BuildContext context)
    {
        return super.visitValuesNode(node, context);
    }
}
