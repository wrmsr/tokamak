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
package com.wrmsr.tokamak.core.plan.search;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeAnnotations;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScope;
import com.wrmsr.tokamak.core.plan.node.PScopeExit;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;
import com.wrmsr.tokamak.core.search.node.SProperty;
import com.wrmsr.tokamak.core.search.node.SSequence;
import com.wrmsr.tokamak.core.search.node.visitor.SNodeVisitor;
import com.wrmsr.tokamak.util.NameGenerator;

public final class SearchTransformation
{
    private SearchTransformation()
    {
    }

    public static PNode transformSearch(PSearch search, NameGenerator nameGenerator)
    {
        search.getSearch().accept(new SNodeVisitor<Void, Void>()
        {
            @Override
            public Void visitProperty(SProperty node, Void context)
            {
                return super.visitProperty(node, context);
            }

            @Override
            public Void visitSequence(SSequence node, Void context)
            {
                return super.visitSequence(node, context);
            }
        }, null);

        String scopeName = nameGenerator.get("searchScope");

        PScopeExit scopeExit = new PScopeExit(
                nameGenerator.get("searchScopeExit"),
                PNodeAnnotations.empty(),
                search.getSource(),
                scopeName);

        PSearch newSearch = new PSearch(
                search.getName(),
                search.getAnnotations(),
                scopeExit,
                search.getSearch(),
                search.getOutputField(),
                search.getOutputType());

        PProject project = new PProject(
                nameGenerator.get("searchScopeDrop"),
                PNodeAnnotations.empty(),
                newSearch,
                PProjection.only(ImmutableList.of(search.getOutputField())));

        PScope scope = new PScope(
                scopeName,
                PNodeAnnotations.empty(),
                project);

        PJoin join = new PJoin(
                nameGenerator.get("searchScopeJoin"),
                PNodeAnnotations.empty(),
                ImmutableList.of(
                        new PJoin.Branch(search.getSource(), ImmutableList.of()),
                        new PJoin.Branch(scope, ImmutableList.of())
                ),
                PJoin.Mode.FULL);

        return join;
    }

    public static PNode transformSearches(Plan plan)
    {
        return plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitSearch(PSearch node, Void context)
            {
                return transformSearch(node, plan.getNodeNameGenerator());
            }
        }, null);
    }
}
