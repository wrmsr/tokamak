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
package com.wrmsr.tokamak.core.parse.transform;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.parse.analysis.ScopeAnalysis;
import com.wrmsr.tokamak.core.parse.node.TAllSelectItem;
import com.wrmsr.tokamak.core.parse.node.TQualifiedName;
import com.wrmsr.tokamak.core.parse.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.parse.node.TNode;
import com.wrmsr.tokamak.core.parse.node.visitor.TNodeRewriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SymbolResolution
{
    private SymbolResolution()
    {
    }

    public static TNode resolveSymbols(TNode node, Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        ScopeAnalysis sa = ScopeAnalysis.analyze(node, catalog, defaultSchema);

        return node.accept(new TNodeRewriter<Void>()
        {
            @Override
            public TNode visitAllSelectItem(TAllSelectItem treeNode, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public TNode visitQualifiedNameExpression(TQualifiedNameExpression treeNode, Void context)
            {
                ScopeAnalysis.SymbolRef sr = checkNotNull(sa.getSymbolRefsByNode().get(treeNode));
                List<String> parts = treeNode.getQualifiedName().getParts();

                List<ScopeAnalysis.Symbol> hits = new ArrayList<>();
                hits.addAll(ScopeAnalysis.getScopeMatches(sr));
                if (parts.size() > 1) {
                    hits.addAll(ScopeAnalysis.getSymbolMatches(sr));
                }

                if (hits.size() > 1) {
                    throw new IllegalStateException(String.format("Ambiguous reference: %s", parts));
                }
                else if (hits.isEmpty()) {
                    throw new IllegalStateException(String.format("Unresolved reference: %s", parts));
                }

                ScopeAnalysis.Symbol hit = hits.get(0);

                return new TQualifiedNameExpression(
                        new TQualifiedName(
                                ImmutableList.of(hit.getScope().getName().get(), hit.getName().get())));
            }
        }, null);
    }
}
