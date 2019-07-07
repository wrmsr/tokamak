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

import com.wrmsr.tokamak.materialization.driver.context.DriverContext;
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
        extends NodeVisitor<NodeOutput, DriverContext>
{
    @Override
    protected NodeOutput visitNode(Node node, DriverContext context)
    {
        throw new IllegalStateException();
    }

    @Override
    public NodeOutput visitCrossJoinNode(CrossJoinNode node, DriverContext context)
    {
        return super.visitCrossJoinNode(node, context);
    }

    @Override
    public NodeOutput visitEquijoinNode(EquijoinNode node, DriverContext context)
    {
        return super.visitEquijoinNode(node, context);
    }

    @Override
    public NodeOutput visitFilterNode(FilterNode node, DriverContext context)
    {
        return super.visitFilterNode(node, context);
    }

    @Override
    public NodeOutput visitListAggregateNode(ListAggregateNode node, DriverContext context)
    {
        return super.visitListAggregateNode(node, context);
    }

    @Override
    public NodeOutput visitLookupJoinNode(LookupJoinNode node, DriverContext context)
    {
        return super.visitLookupJoinNode(node, context);
    }

    @Override
    public NodeOutput visitPersistNode(PersistNode node, DriverContext context)
    {
        return super.visitPersistNode(node, context);
    }

    @Override
    public NodeOutput visitProjectNode(ProjectNode node, DriverContext context)
    {
        return super.visitProjectNode(node, context);
    }

    @Override
    public NodeOutput visitScanNode(ScanNode node, DriverContext context)
    {
        return super.visitScanNode(node, context);
    }

    @Override
    public NodeOutput visitUnionNode(UnionNode node, DriverContext context)
    {
        return super.visitUnionNode(node, context);
    }

    @Override
    public NodeOutput visitUnnestNode(UnnestNode node, DriverContext context)
    {
        return super.visitUnnestNode(node, context);
    }

    @Override
    public NodeOutput visitValuesNode(ValuesNode node, DriverContext context)
    {
        return super.visitValuesNode(node, context);
    }
}
