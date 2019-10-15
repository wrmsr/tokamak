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
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PCrossJoin;
import com.wrmsr.tokamak.core.plan.node.PEquiJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;

import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;

public abstract class PNodeRewriter<C>
        extends CachingPNodeVisitor<PNode, C>
{
    public String visitNodeName(String name, C context)
    {
        return name;
    }

    @Override
    public PNode visitCache(PCache node, C context)
    {
        return new PCache(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                node.getSource());
    }

    @Override
    public PNode visitCrossJoin(PCrossJoin node, C context)
    {
        return new PCrossJoin(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                node.getSources().stream().map(n -> process(n, context)).collect(toImmutableList()),
                node.getMode());
    }

    @Override
    public PNode visitEquiJoin(PEquiJoin node, C context)
    {
        return new PEquiJoin(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                node.getBranches().stream()
                        .map(b -> new PEquiJoin.Branch(
                                process(b.getNode(), context),
                                b.getFields()))
                        .collect(toImmutableList()),
                node.getMode());
    }

    @Override
    public PNode visitFilter(PFilter node, C context)
    {
        return new PFilter(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                process(node.getSource(), context),
                node.getFunction(),
                node.getArgs(),
                node.getLinkage());
    }

    @Override
    public PNode visitGroup(PGroup node, C context)
    {
        return new PGroup(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                process(node.getSource(), context),
                node.getKeyFields(),
                node.getListField());
    }

    @Override
    public PNode visitLookupJoin(PLookupJoin node, C context)
    {
        return new PLookupJoin(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                process(node.getSource(), context),
                node.getSourceKeyFields(),
                node.getBranches().stream()
                        .map(b -> new PLookupJoin.Branch(
                                process(b.getNode(), context),
                                b.getFields()))
                        .collect(toImmutableList()));
    }

    @Override
    public PNode visitProject(PProject node, C context)
    {
        return new PProject(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                process(node.getSource(), context),
                node.getProjection());
    }

    @Override
    public PNode visitScan(PScan node, C context)
    {
        return new PScan(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                node.getSchemaTable(),
                node.getFields().getTypesByName());
    }

    @Override
    public PNode visitSearch(PSearch node, C context)
    {
        return new PSearch(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                process(node.getSource(), context),
                node.getSearch(),
                node.getOutputField(),
                node.getOutputType());
    }

    @Override
    public PNode visitState(PState node, C context)
    {
        return new PState(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                process(node.getSource(), context),
                node.getWriterTargets(),
                node.getDenormalization(),
                node.getInvalidations().entrySet().stream()
                        .collect(ImmutableMap.toImmutableMap(e -> visitNodeName(e.getKey(), context), Map.Entry::getValue)),
                node.getLinkageMasks().entrySet().stream()
                        .collect(ImmutableMap.toImmutableMap(e -> visitNodeName(e.getKey(), context), Map.Entry::getValue)),
                node.getLockOverride().map(lo -> new PState.LockOverride(visitNodeName(lo.getNode(), context), lo.getField(), false)));
    }

    @Override
    public PNode visitUnion(PUnion node, C context)
    {
        return new PUnion(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                node.getSources().stream().map(n -> process(n, context)).collect(toImmutableList()),
                node.getIndexField());
    }

    @Override
    public PNode visitUnnest(PUnnest node, C context)
    {
        return new PUnnest(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                process(node.getSource(), context),
                node.getListField(),
                node.getUnnestedFields(),
                node.getIndexField());
    }

    @Override
    public PNode visitValues(PValues node, C context)
    {
        return new PValues(
                visitNodeName(node.getName(), context),
                node.getAnnotations(),
                node.getFields().getTypesByName(),
                node.getValues(),
                node.getIndexField(),
                node.isWeak());
    }
}
