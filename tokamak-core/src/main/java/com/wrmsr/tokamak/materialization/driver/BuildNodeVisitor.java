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
package com.wrmsr.tokamak.materialization.driver;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.materialization.api.Payload;
import com.wrmsr.tokamak.materialization.node.CrossJoinNode;
import com.wrmsr.tokamak.materialization.node.EquijoinNode;
import com.wrmsr.tokamak.materialization.node.FilterNode;
import com.wrmsr.tokamak.materialization.node.ListAggregateNode;
import com.wrmsr.tokamak.materialization.node.LookupJoinNode;
import com.wrmsr.tokamak.materialization.node.Node;
import com.wrmsr.tokamak.materialization.node.PersistNode;
import com.wrmsr.tokamak.materialization.node.ProjectNode;
import com.wrmsr.tokamak.materialization.node.ScanNode;
import com.wrmsr.tokamak.materialization.node.UnionNode;
import com.wrmsr.tokamak.materialization.node.UnnestNode;
import com.wrmsr.tokamak.materialization.node.ValuesNode;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;

import java.util.List;

public class BuildNodeVisitor
        extends NodeVisitor<List<NodeOutput>, BuildContext>
{
    @Override
    protected List<NodeOutput> visitNode(Node node, BuildContext context)
    {
        throw new IllegalStateException();
    }

    @Override
    public List<NodeOutput> visitCrossJoinNode(CrossJoinNode node, BuildContext context)
    {
        return super.visitCrossJoinNode(node, context);
    }

    @Override
    public List<NodeOutput> visitEquijoinNode(EquijoinNode node, BuildContext context)
    {
        return super.visitEquijoinNode(node, context);
    }

    @Override
    public List<NodeOutput> visitFilterNode(FilterNode node, BuildContext context)
    {
        ImmutableList.Builder<NodeOutput> ret = ImmutableList.builder();
        for (Payload payload : context.getDriverContext().build(node.getSource(), context.getKey())) {
            if (node.getPredicate().test(payload.getAttributes())) {
                ret.add(new NodeOutput(new Payload(payload.getId(), payload.getAttributes()), ImmutableList.of(payload)));
            }
            else {
                ret.add(new NodeOutput(new Payload(payload.getId(), null), ImmutableList.of(payload)));
            }
        }
        return ret.build();
    }

    @Override
    public List<NodeOutput> visitListAggregateNode(ListAggregateNode node, BuildContext context)
    {
        return super.visitListAggregateNode(node, context);
    }

    @Override
    public List<NodeOutput> visitLookupJoinNode(LookupJoinNode node, BuildContext context)
    {
        return super.visitLookupJoinNode(node, context);
    }

    @Override
    public List<NodeOutput> visitPersistNode(PersistNode node, BuildContext context)
    {
        return super.visitPersistNode(node, context);
    }

    @Override
    public List<NodeOutput> visitProjectNode(ProjectNode node, BuildContext context)
    {
        return super.visitProjectNode(node, context);
    }

    @Override
    public List<NodeOutput> visitScanNode(ScanNode node, BuildContext context)
    {
        return super.visitScanNode(node, context);
    }

    @Override
    public List<NodeOutput> visitUnionNode(UnionNode node, BuildContext context)
    {
        return super.visitUnionNode(node, context);
    }

    @Override
    public List<NodeOutput> visitUnnestNode(UnnestNode node, BuildContext context)
    {
        return super.visitUnnestNode(node, context);
    }

    @Override
    public List<NodeOutput> visitValuesNode(ValuesNode node, BuildContext context)
    {
        return super.visitValuesNode(node, context);
    }
}
