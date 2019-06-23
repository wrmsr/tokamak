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

public class BuildNodeVisitor
        extends NodeVisitor<NodeOutput, Void>
{
    @Override
    protected Void visitNode(Node node, NodeOutput context)
    {
        throw new IllegalStateException();
    }

    @Override
    public Void visitCrossJoinNode(CrossJoinNode node, NodeOutput context)
    {
        return super.visitCrossJoinNode(node, context);
    }

    @Override
    public Void visitEquijoinNode(EquijoinNode node, NodeOutput context)
    {
        return super.visitEquijoinNode(node, context);
    }

    @Override
    public Void visitFilterNode(FilterNode node, NodeOutput context)
    {
        return super.visitFilterNode(node, context);
    }

    @Override
    public Void visitListAggregateNode(ListAggregateNode node, NodeOutput context)
    {
        return super.visitListAggregateNode(node, context);
    }

    @Override
    public Void visitLookupJoinNode(LookupJoinNode node, NodeOutput context)
    {
        return super.visitLookupJoinNode(node, context);
    }

    @Override
    public Void visitPersistNode(PersistNode node, NodeOutput context)
    {
        return super.visitPersistNode(node, context);
    }

    @Override
    public Void visitProjectNode(ProjectNode node, NodeOutput context)
    {
        return super.visitProjectNode(node, context);
    }

    @Override
    public Void visitScanNode(ScanNode node, NodeOutput context)
    {
        return super.visitScanNode(node, context);
    }

    @Override
    public Void visitUnionNode(UnionNode node, NodeOutput context)
    {
        return super.visitUnionNode(node, context);
    }

    @Override
    public Void visitUnnestNode(UnnestNode node, NodeOutput context)
    {
        return super.visitUnnestNode(node, context);
    }

    @Override
    public Void visitValuesNode(ValuesNode node, NodeOutput context)
    {
        return super.visitValuesNode(node, context);
    }
}
