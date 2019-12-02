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
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScope;
import com.wrmsr.tokamak.core.plan.node.PScopeExit;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.search.node.SProperty;
import com.wrmsr.tokamak.core.search.node.SSequence;
import com.wrmsr.tokamak.core.search.node.visitor.SNodeVisitor;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.NameGenerator;

public final class SearchTransformation
{
    private SearchTransformation()
    {
    }

    public static PNode transformSearch(PSearch search, NameGenerator nameGenerator)
    {
        String scopeName = nameGenerator.get("searchScope");

        PScopeExit scopeExit = new PScopeExit(
                nameGenerator.get("searchScopeExit"),
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                search.getSource(),
                scopeName);

        PNode newSearch = search.getSearch().accept(new SNodeVisitor<PNode, PNode>()
        {
            @Override
            protected PNode visitNode(SNode node, PNode context)
            {
                return new PSearch(
                        search.getName(),
                        AnnotationCollection.of(),
                        AnnotationCollectionMap.of(),
                        context,
                        node,
                        search.getOutputField(),
                        search.getOutputType());
            }

            @Override
            public PNode visitProperty(SProperty node, PNode context)
            {
                return super.visitProperty(node, context);
            }

            @Override
            public PNode visitSequence(SSequence node, PNode context)
            {
                return super.visitSequence(node, context);
            }
        }, scopeExit);

        PProject project = new PProject(
                nameGenerator.get("searchScopeDrop"),
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                newSearch,
                PProjection.only(ImmutableList.of(search.getOutputField())));

        PScope scope = new PScope(
                scopeName,
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                project);

        PJoin join = new PJoin(
                nameGenerator.get("searchScopeJoin"),
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
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
