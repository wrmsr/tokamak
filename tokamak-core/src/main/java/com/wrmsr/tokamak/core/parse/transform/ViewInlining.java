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

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.View;
import com.wrmsr.tokamak.core.parse.AstBuilding;
import com.wrmsr.tokamak.core.parse.Parsing;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.AstRewriter;

import java.util.Optional;

public final class ViewInlining
{
    private ViewInlining()
    {
    }

    public TreeNode inlineViews(TreeNode root, Catalog catalog)
    {
        return root.accept(new AstRewriter<Void>()
        {
            @Override
            public TreeNode visitTableName(TableName treeNode, Void context)
            {
                if (treeNode.getQualifiedName().getParts().size() == 1) {
                    String name = treeNode.getQualifiedName().getParts().get(0);
                    Optional<View> viewOpt = catalog.getViewOptional(name);
                    if (viewOpt.isPresent()) {
                        View view = viewOpt.get();
                        SqlParser viewParser = Parsing.parse(view.getSql());
                        Select viewSelect = (Select) AstBuilding.build(viewParser.statement());
                        Select processedSelect = (Select) viewSelect.accept(this, context);
                        return new SubqueryRelation(processedSelect);
                    }
                }
                return super.visitTableName(treeNode, context);
            }
        }, null);
    }
}
