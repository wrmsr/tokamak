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

import com.wrmsr.tokamak.core.catalog.View;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.TreeParsing;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableName;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeRewriter;

import java.util.Optional;

public final class ViewInlining
{
    private ViewInlining()
    {
    }

    public static TNode inlineViews(TNode root, ParsingContext parsingContext)
    {
        return root.accept(new TNodeRewriter<Void>()
        {
            @Override
            public TNode visitTableName(TTableName treeNode, Void context)
            {
                if (treeNode.getQualifiedName().getParts().size() == 1) {
                    String name = treeNode.getQualifiedName().getParts().get(0);
                    Optional<View> viewOpt = parsingContext.getCatalog().get().getViewOptional(name);
                    if (viewOpt.isPresent()) {
                        View view = viewOpt.get();
                        SqlParser viewParser = TreeParsing.parse(view.getSql());
                        TSelect viewSelect = (TSelect) TreeParsing.build(viewParser.statement());
                        TSelect processedSelect = (TSelect) process(viewSelect, context);
                        return new TSubqueryRelation(processedSelect);
                    }
                }
                return super.visitTableName(treeNode, context);
            }
        }, null);
    }
}
