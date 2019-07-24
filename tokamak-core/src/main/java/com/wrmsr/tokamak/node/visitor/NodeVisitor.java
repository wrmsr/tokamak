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
package com.wrmsr.tokamak.node.visitor;

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

public abstract class NodeVisitor<R, C>
{
    protected R visitNode(Node node, C context)
    {
        return null;
    }

    public R visitCrossJoinNode(CrossJoinNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitEquijoinNode(EquijoinNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitFilterNode(FilterNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitListAggregateNode(ListAggregateNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitLookupJoinNode(LookupJoinNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitPersistNode(PersistNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitProjectNode(ProjectNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitScanNode(ScanNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitUnionNode(UnionNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitUnnestNode(UnnestNode node, C context)
    {
        return visitNode(node, context);
    }

    public R visitValuesNode(ValuesNode node, C context)
    {
        return visitNode(node, context);
    }
}
