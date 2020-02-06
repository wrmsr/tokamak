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
package com.wrmsr.tokamak.core.tree.transform;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.analysis.SymbolAnalysis;
import com.wrmsr.tokamak.core.tree.node.TAllSelectItem;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeRewriter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SymbolResolution
{
    private SymbolResolution()
    {
    }

    public static TNode resolveSymbols(TNode node, ParsingContext parsingContext)
    {
        SymbolAnalysis sa = SymbolAnalysis.analyze(node, parsingContext);

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
                SymbolAnalysis.SymbolRef sr = checkNotNull(sa.getSymbolRefsByNode().get(treeNode));
                List<String> parts = treeNode.getQualifiedName().getParts();

                List<SymbolAnalysis.Symbol> hits = new ArrayList<>();
                hits.addAll(SymbolAnalysis.getSymbolScopeMatches(sr));
                if (parts.size() > 1) {
                    hits.addAll(SymbolAnalysis.getSymbolMatches(sr));
                }

                if (hits.size() > 1) {
                    throw new IllegalStateException(String.format("Ambiguous reference: %s", parts));
                }
                else if (hits.isEmpty()) {
                    throw new IllegalStateException(String.format("Unresolved reference: %s", parts));
                }

                SymbolAnalysis.Symbol hit = hits.get(0);

                return new TQualifiedNameExpression(
                        new TQualifiedName(
                                ImmutableList.of(hit.getSymbolScope().getName().get(), hit.getName().get())));
            }
        }, null);
    }
}
