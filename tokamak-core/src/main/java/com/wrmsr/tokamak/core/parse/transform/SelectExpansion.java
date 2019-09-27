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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.tree.AliasedRelation;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.Expression;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SelectItem;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.AstRewriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class SelectExpansion
{
    private SelectExpansion()
    {
    }

    public static TreeNode expandSelects(TreeNode node, Catalog catalog, Optional<String> defaultSchema)
    {
        return node.accept(new AstRewriter<Void>()
        {
            private final Map<TreeNode, Set<String>> fieldSetsByNode = new HashMap<>();

            @Override
            public TreeNode visitSelect(Select treeNode, Void context)
            {
                List<AliasedRelation> relations = treeNode.getRelations().stream()
                        .map(r -> (AliasedRelation) r.accept(this, context))
                        .collect(toImmutableList());

                List<SelectItem> items = new ArrayList<>();
                Set<String> fields = new LinkedHashSet<>();
                int numAnon = 0;

                for (SelectItem item : treeNode.getItems()) {
                    if (item instanceof AllSelectItem) {

                    }
                    else if (item instanceof ExpressionSelectItem) {

                    }
                    else {

                    }
                }

                Select ret = new Select(
                        items,
                        relations,
                        treeNode.getWhere().map(w -> (Expression) w.accept(this, context)));
                fieldSetsByNode.put(ret, ImmutableSet.copyOf(fields));
                return ret;
            }

            @Override
            public TreeNode visitSubqueryRelation(SubqueryRelation treeNode, Void context)
            {
                return super.visitSubqueryRelation(treeNode, context);
            }

            @Override
            public TreeNode visitTableName(TableName treeNode, Void context)
            {
                TableName ret = (TableName) super.visitTableName(treeNode, context);
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);
                Table table = catalog.getSchemaTable(schemaTable);
                fieldSetsByNode.put(ret, ImmutableSet.copyOf(table.getRowLayout().getFieldNames()));
                return ret;
            }
        }, null);
    }
}
