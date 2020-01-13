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

import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeRewriter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

public final class LetFunctionRewriting
{
    private LetFunctionRewriting()
    {
    }

    public static TNode rewriteLetFunctions(TNode node)
    {
        return node.accept(new TNodeRewriter<Void>()
        {
            @Override
            public TNode visitFunctionCallExpression(TFunctionCallExpression node, Void context)
            {
                checkState(!node.getName().equals("let"));
                return super.visitFunctionCallExpression(node, context);
            }

            @Override
            public TNode visitSelect(TSelect node, Void context)
            {
                TSelect cur = node;
                Set<String> drops = new LinkedHashSet<>();
                List<TSelectItem> items = new ArrayList<>();

                return new TSelect(
                        immutableMapItems(node.getItems(), i -> (TSelectItem) process(i, context)),
                        immutableMapItems(node.getRelations(), r -> (TAliasedRelation) process(r, context)),
                        node.getWhere().map(w -> (TExpression) process(w, context)));
            }
        }, null);
    }
}
