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
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Row;
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
        for (Row row : context.getDriverContext().build(node.getSource(), context.getKey())) {
            if (node.getPredicate().test(row)) {
                ret.add(new BuildOutput(new Row(row.getId(), row.getAttributes()), ImmutableList.of(row)));
            }
            else {
                ret.add(new BuildOutput(new Row(row.getId(), null), ImmutableList.of(row)));
            }
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
        // Scanner scanner = new Scanner(node.getTable(), node.getFields().keySet());

        if (context.getKey() instanceof IdKey) {

        }
        else {
            throw new IllegalArgumentException(context.getKey().toString());
        }

        // scanner.scan(
        //         context.getDriverContext().getJdbiHandle(),
        //         context.getKey().)

        throw new UnsupportedOperationException();
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
