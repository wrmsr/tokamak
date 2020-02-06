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

import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.analysis.SymbolAnalysis;
import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeRewriter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

public final class DropFunctionRewriting
{
    private DropFunctionRewriting()
    {
    }

    public static TNode rewriteDropFunctions(TNode node, ParsingContext parsingContext)
    {
        SymbolAnalysis symbolAnalysis = SymbolAnalysis.analyze(node, parsingContext);

        return node.accept(new TNodeRewriter<Void>()
        {
            @Override
            public TNode visitFunctionCallExpression(TFunctionCallExpression node, Void context)
            {
                checkState(!node.getName().equals("drop"));
                return super.visitFunctionCallExpression(node, context);
            }

            @Override
            public TNode visitSelect(TSelect node, Void context)
            {
                SymbolAnalysis.SymbolScope symScope = symbolAnalysis.getSymbolScope(node).get();

                List<TSelectItem> items = new ArrayList<>();
                node.getItems().forEach(i -> {
                    checkState(!cache.containsKey(i));
                    checkState(i instanceof TExpressionSelectItem);
                    TExpressionSelectItem exprItem = (TExpressionSelectItem) i;
                    if (exprItem.getExpression() instanceof TFunctionCallExpression) {
                        TFunctionCallExpression fce = (TFunctionCallExpression) exprItem.getExpression();
                        if (fce.getName().equals("drop")) {
                        }
                    }
                    items.add((TSelectItem) process(exprItem, context));
                });

                return new TSelect(
                        items,
                        immutableMapItems(node.getRelations(), r -> (TAliasedRelation) process(r, context)),
                        node.getWhere().map(w -> (TExpression) process(w, context)));
            }
        }, null);
    }
}
