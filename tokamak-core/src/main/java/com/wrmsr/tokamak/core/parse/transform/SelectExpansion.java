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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.tree.AliasedRelation;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.Expression;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.Relation;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SelectItem;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.AstRewriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollections.histogram;

public final class SelectExpansion
{
    private SelectExpansion()
    {
    }

    private static List<AliasedRelation> addRelationAliases(List<AliasedRelation> aliasedRelations)
    {
        Set<String> seen = new HashSet<>();
        List<AliasedRelation> ret = new ArrayList<>();
        int numAnon = 0;

        Map<String, Long> tableNameCounts = histogram(aliasedRelations.stream()
                .map(AliasedRelation::getRelation)
                .filter(TableName.class::isInstance)
                .map(TableName.class::cast)
                .map(TableName::getQualifiedName)
                .map(QualifiedName::getLast));
        Map<String, Integer> dupeTableNameCounts = new HashMap<>();

        for (AliasedRelation ar : aliasedRelations) {
            if (!ar.getAlias().isPresent()) {
                Relation r = ar.getRelation();
                String alias;

                if (r instanceof SubqueryRelation) {
                    alias = "_" + (numAnon++);
                }

                else if (r instanceof TableName) {
                    TableName tn = (TableName) r;
                    String name = tn.getQualifiedName().getLast();
                    if (tableNameCounts.get(name) > 1) {
                        int num = dupeTableNameCounts.getOrDefault(name, 0);
                        dupeTableNameCounts.put(name, num + 1);
                        alias = name + "_" + num;
                    }
                    else {
                        alias = name;
                    }
                }

                else {
                    throw new IllegalStateException(Objects.toString(r));
                }

                ar = new AliasedRelation(
                        ar.getRelation(),
                        Optional.of(alias));
            }

            checkState(!seen.contains(ar.getAlias().get()));
            seen.add(ar.getAlias().get());
            ret.add(ar);
        }

        return ImmutableList.copyOf(ret);
    }

    private static List<SelectItem> addItemLabels(
            List<SelectItem> items,
            List<AliasedRelation> relations,
            Map<TreeNode, Set<String>> fieldSetsByNode)
    {
        Set<String> seen = new HashSet<>();
        List<SelectItem> ret = new ArrayList<>();
        int numAnon = 0;

        Map<String, Long> relationFieldCounts = histogram(relations.stream()
                .map(AliasedRelation::getRelation)
                .map(fieldSetsByNode::get)
                .flatMap(Set::stream));

        for (SelectItem item : items) {
            if (item instanceof AllSelectItem) {
                Map<String, Integer> dupeCounts = new HashMap<>();
                for (AliasedRelation relation : relations) {
                    Set<String> relationFields = fieldSetsByNode.get(relation.getRelation());
                    for (String relationField : relationFields) {
                        String label;
                        if (relationFieldCounts.get(relationField) > 1) {
                            int num = dupeCounts.getOrDefault(relationField, 0);
                            dupeCounts.put(relationField, num + 1);
                            label = relationField + '_' + num;
                        }
                        else {
                            label = relationField;
                        }

                        checkState(!seen.contains(label));
                        seen.add(label);
                        ret.add(
                                new ExpressionSelectItem(
                                        new QualifiedName(
                                                ImmutableList.of(relation.getAlias().get(), label)),
                                        Optional.of(label)));
                    }
                }
            }

            else if (item instanceof ExpressionSelectItem) {
                ExpressionSelectItem eitem = (ExpressionSelectItem) item;
                String label;
                if (eitem.getLabel().isPresent()) {
                    label = eitem.getLabel().get();
                }
                else if (eitem.getExpression() instanceof QualifiedName) {
                    label = ((QualifiedName) eitem.getExpression()).getLast();
                }
                else {
                    label = "_" + (numAnon++);
                }

                checkState(!seen.contains(label));
                seen.add(label);
                ret.add(
                        new ExpressionSelectItem(
                                eitem.getExpression(),
                                Optional.of(label)));
            }

            else {
                throw new IllegalStateException(Objects.toString(item));
            }
        }

        return ret;
    }

    public static TreeNode expandSelects(TreeNode node, Catalog catalog, Optional<String> defaultSchema)
    {
        return node.accept(new AstRewriter<Void>()
        {
            private final Map<TreeNode, Set<String>> fieldSetsByNode = new HashMap<>();

            @Override
            public TreeNode visitSelect(Select treeNode, Void context)
            {
                List<AliasedRelation> relations = addRelationAliases(
                        treeNode.getRelations().stream()
                                .map(r -> (AliasedRelation) r.accept(this, context))
                                .collect(toImmutableList()));

                List<SelectItem> items = addItemLabels(
                        treeNode.getItems(),
                        relations,
                        fieldSetsByNode);

                Set<String> fields = items.stream()
                        .map(ExpressionSelectItem.class::cast)
                        .map(ExpressionSelectItem::getLabel)
                        .map(Optional::get)
                        .collect(toImmutableSet());

                Select ret = new Select(
                        items,
                        relations,
                        treeNode.getWhere().map(w -> (Expression) w.accept(this, context)));

                fieldSetsByNode.put(ret, fields);

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
