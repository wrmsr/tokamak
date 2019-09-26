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
package com.wrmsr.tokamak.core.plan.node.visitor;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.plan.node.CacheNode;
import com.wrmsr.tokamak.core.plan.node.CrossJoinNode;
import com.wrmsr.tokamak.core.plan.node.EquijoinNode;
import com.wrmsr.tokamak.core.plan.node.FilterNode;
import com.wrmsr.tokamak.core.plan.node.ListAggregateNode;
import com.wrmsr.tokamak.core.plan.node.LockOverride;
import com.wrmsr.tokamak.core.plan.node.LookupJoinNode;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.StateNode;
import com.wrmsr.tokamak.core.plan.node.ProjectNode;
import com.wrmsr.tokamak.core.plan.node.ScanNode;
import com.wrmsr.tokamak.core.plan.node.UnionNode;
import com.wrmsr.tokamak.core.plan.node.UnnestNode;
import com.wrmsr.tokamak.core.plan.node.ValuesNode;

import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;

public abstract class NodeRewriter<C>
        extends CachingNodeVisitor<Node, C>
{
    public String visitNodeName(String name, C context)
    {
        return name;
    }

    @Override
    public Node visitCacheNode(CacheNode node, C context)
    {
        return new CacheNode(
                visitNodeName(node.getName(), context),
                node.getSource());
    }

    @Override
    public Node visitCrossJoinNode(CrossJoinNode node, C context)
    {
        return new CrossJoinNode(
                visitNodeName(node.getName(), context),
                node.getSources().stream().map(n -> get(n, context)).collect(toImmutableList()),
                node.getMode());
    }

    @Override
    public Node visitEquijoinNode(EquijoinNode node, C context)
    {
        return new EquijoinNode(
                visitNodeName(node.getName(), context),
                node.getBranches().stream()
                        .map(b -> new EquijoinNode.Branch(
                                get(b.getNode(), context),
                                b.getFields()))
                        .collect(toImmutableList()),
                node.getMode());
    }

    @Override
    public Node visitFilterNode(FilterNode node, C context)
    {
        return new FilterNode(
                visitNodeName(node.getName(), context),
                get(node.getSource(), context),
                node.getFunction(),
                node.getArgs(),
                node.isUnlinked());
    }

    @Override
    public Node visitListAggregateNode(ListAggregateNode node, C context)
    {
        return new ListAggregateNode(
                visitNodeName(node.getName(), context),
                get(node.getSource(), context),
                node.getGroupField(),
                node.getListField());
    }

    @Override
    public Node visitLookupJoinNode(LookupJoinNode node, C context)
    {
        return new LookupJoinNode(
                visitNodeName(node.getName(), context),
                get(node.getSource(), context),
                node.getSourceKeyFields(),
                node.getBranches().stream()
                        .map(b -> new LookupJoinNode.Branch(
                                get(b.getNode(), context),
                                b.getFields()))
                        .collect(toImmutableList()));
    }

    @Override
    public Node visitPersistNode(StateNode node, C context)
    {
        return new StateNode(
                visitNodeName(node.getName(), context),
                get(node.getSource(), context),
                node.getIdFields(),
                node.getWriterTargets(),
                node.isDenormalized(),
                node.getInvalidations().entrySet().stream()
                        .collect(ImmutableMap.toImmutableMap(e -> visitNodeName(e.getKey(), context), Map.Entry::getValue)),
                node.getLinkageMasks().entrySet().stream()
                        .collect(ImmutableMap.toImmutableMap(e -> visitNodeName(e.getKey(), context), Map.Entry::getValue)),
                node.getLockOverride().map(lo -> new LockOverride(visitNodeName(lo.getNode(), context), lo.getField(), false)));
    }

    @Override
    public Node visitProjectNode(ProjectNode node, C context)
    {
        return new ProjectNode(
                visitNodeName(node.getName(), context),
                get(node.getSource(), context),
                node.getProjection());
    }

    @Override
    public Node visitScanNode(ScanNode node, C context)
    {
        return new ScanNode(
                visitNodeName(node.getName(), context),
                node.getSchemaTable(),
                node.getFields(),
                node.getIdFields(),
                node.getIdNodes());
    }

    @Override
    public Node visitUnionNode(UnionNode node, C context)
    {
        return new UnionNode(
                visitNodeName(node.getName(), context),
                node.getSources().stream().map(n -> get(n, context)).collect(toImmutableList()),
                node.getIndexField());
    }

    @Override
    public Node visitUnnestNode(UnnestNode node, C context)
    {
        return new UnnestNode(
                visitNodeName(node.getName(), context),
                get(node.getSource(), context),
                node.getListField(),
                node.getUnnestedFields(),
                node.getIndexField());
    }

    @Override
    public Node visitValuesNode(ValuesNode node, C context)
    {
        return new ValuesNode(
                visitNodeName(node.getName(), context),
                node.getFields(),
                node.getValues(),
                node.getIndexField(),
                node.isWeak());
    }
}
