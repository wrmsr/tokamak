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

import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PExtract;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PLookup;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeAnnotations;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PScope;
import com.wrmsr.tokamak.core.plan.node.PScopeExit;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnify;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;

import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public abstract class PNodeRewriter<C>
        extends CachingPNodeVisitor<PNode, C>
{
    protected String visitNodeName(String name, C context)
    {
        return name;
    }

    protected PInvalidations visitInvalidations(PInvalidations invalidations, C context)
    {
        return new PInvalidations(
                invalidations.getEntriesByNode().entrySet().stream()
                        .collect(toImmutableMap(e -> visitNodeName(e.getKey(), context), Map.Entry::getValue)));
    }

    protected PNodeAnnotations visitNodeAnnotations(PNode node, PNodeAnnotations annotations, C context)
    {
        return annotations;
    }

    @Override
    public PNode visitCache(PCache node, C context)
    {
        return new PCache(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context));
    }

    @Override
    public PNode visitExtract(PExtract node, C context)
    {
        return new PExtract(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getSourceField(),
                node.getStructMember(),
                node.getOutputField());
    }

    @Override
    public PNode visitFilter(PFilter node, C context)
    {
        return new PFilter(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getFunction(),
                node.getArgs(),
                node.getLinking());
    }

    @Override
    public PNode visitGroup(PGroup node, C context)
    {
        return new PGroup(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getKeyFields(),
                node.getListField());
    }

    @Override
    public PNode visitJoin(PJoin node, C context)
    {
        return new PJoin(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                node.getBranches().stream()
                        .map(b -> new PJoin.Branch(
                                process(b.getNode(), context),
                                b.getFields()))
                        .collect(toImmutableList()),
                node.getMode());
    }

    @Override
    public PNode visitLookup(PLookup node, C context)
    {
        return new PLookup(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getSourceKeyFields(),
                node.getBranches().stream()
                        .map(b -> new PLookup.Branch(
                                process(b.getNode(), context),
                                b.getFields()))
                        .collect(toImmutableList()));
    }

    @Override
    public PNode visitOutput(POutput node, C context)
    {
        return new POutput(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getTargets());
    }

    @Override
    public PNode visitProject(PProject node, C context)
    {
        return new PProject(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getProjection());
    }

    @Override
    public PNode visitScan(PScan node, C context)
    {
        return new PScan(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                node.getSchemaTable(),
                node.getScanFields(),
                visitInvalidations(node.getInvalidations(), context));
    }

    @Override
    public PNode visitScope(PScope node, C context)
    {
        return new PScope(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context));
    }

    @Override
    public PNode visitScopeExit(PScopeExit node, C context)
    {
        return new PScopeExit(
                visitNodeName(node.getScopeName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                visitNodeName(node.getScopeName(), context));
    }

    @Override
    public PNode visitSearch(PSearch node, C context)
    {
        return new PSearch(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
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
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getDenormalization(),
                visitInvalidations(node.getInvalidations(), context));
    }

    @Override
    public PNode visitStruct(PStruct node, C context)
    {
        return new PStruct(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getType(),
                node.getInputFields(),
                node.getOutputField());
    }

    @Override
    public PNode visitUnify(PUnify node, C context)
    {
        return new PUnify(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                process(node.getSource(), context),
                node.getUnifiedFields(),
                node.getOutputField());
    }

    @Override
    public PNode visitUnion(PUnion node, C context)
    {
        return new PUnion(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
                node.getSources().stream().map(n -> process(n, context)).collect(toImmutableList()),
                node.getIndexField());
    }

    @Override
    public PNode visitUnnest(PUnnest node, C context)
    {
        return new PUnnest(
                visitNodeName(node.getName(), context),
                visitNodeAnnotations(node, node.getAnnotations(), context),
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
                visitNodeAnnotations(node, node.getAnnotations(), context),
                node.getFields().getTypesByName(),
                node.getValues(),
                node.getIndexField(),
                node.getStrictness());
    }
}
