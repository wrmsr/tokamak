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
import com.wrmsr.tokamak.core.parse.node.TAliasedRelation;
import com.wrmsr.tokamak.core.parse.node.TAllSelectItem;
import com.wrmsr.tokamak.core.parse.node.TExpression;
import com.wrmsr.tokamak.core.parse.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.node.TQualifiedName;
import com.wrmsr.tokamak.core.parse.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.parse.node.TRelation;
import com.wrmsr.tokamak.core.parse.node.TSelect;
import com.wrmsr.tokamak.core.parse.node.TSelectItem;
import com.wrmsr.tokamak.core.parse.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.parse.node.TTableName;
import com.wrmsr.tokamak.core.parse.node.TNode;
import com.wrmsr.tokamak.core.parse.node.visitor.TNodeRewriter;

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

    private static List<TAliasedRelation> addRelationAliases(List<TAliasedRelation> aliasedRelations)
    {
        Set<String> seen = new HashSet<>();
        List<TAliasedRelation> ret = new ArrayList<>();
        int numAnon = 0;

        Map<String, Long> tableNameCounts = histogram(aliasedRelations.stream()
                .map(TAliasedRelation::getRelation)
                .filter(TTableName.class::isInstance)
                .map(TTableName.class::cast)
                .map(TTableName::getQualifiedName)
                .map(TQualifiedName::getLast));
        Map<String, Integer> dupeTableNameCounts = new HashMap<>();

        for (TAliasedRelation ar : aliasedRelations) {
            if (!ar.getAlias().isPresent()) {
                TRelation r = ar.getRelation();
                String alias;

                if (r instanceof TSubqueryRelation) {
                    alias = "_" + (numAnon++);
                }

                else if (r instanceof TTableName) {
                    TTableName tn = (TTableName) r;
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

                ar = new TAliasedRelation(
                        ar.getRelation(),
                        Optional.of(alias));
            }

            checkState(!seen.contains(ar.getAlias().get()));
            seen.add(ar.getAlias().get());
            ret.add(ar);
        }

        return ImmutableList.copyOf(ret);
    }

    private static List<TSelectItem> addItemLabels(
            List<TSelectItem> items,
            List<TAliasedRelation> relations,
            Map<TNode, Set<String>> fieldSetsByNode)
    {
        Set<String> seen = new HashSet<>();
        List<TSelectItem> ret = new ArrayList<>();
        int numAnon = 0;

        Map<String, Long> relationFieldCounts = histogram(relations.stream()
                .map(TAliasedRelation::getRelation)
                .map(fieldSetsByNode::get)
                .flatMap(Set::stream));

        for (TSelectItem item : items) {
            if (item instanceof TAllSelectItem) {
                Map<String, Integer> dupeCounts = new HashMap<>();
                for (TAliasedRelation relation : relations) {
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
                                new TExpressionSelectItem(
                                        new TQualifiedNameExpression(
                                                new TQualifiedName(
                                                        ImmutableList.of(relation.getAlias().get(), relationField))),
                                        Optional.of(label)));
                    }
                }
            }

            else if (item instanceof TExpressionSelectItem) {
                TExpressionSelectItem eitem = (TExpressionSelectItem) item;
                String label;
                if (eitem.getLabel().isPresent()) {
                    label = eitem.getLabel().get();
                }
                else if (eitem.getExpression() instanceof TQualifiedNameExpression) {
                    label = ((TQualifiedNameExpression) eitem.getExpression()).getQualifiedName().getLast();
                }
                else {
                    label = "_" + (numAnon++);
                }

                checkState(!seen.contains(label));
                seen.add(label);
                ret.add(
                        new TExpressionSelectItem(
                                eitem.getExpression(),
                                Optional.of(label)));
            }

            else {
                throw new IllegalStateException(Objects.toString(item));
            }
        }

        return ret;
    }

    public static TNode expandSelects(TNode node, Catalog catalog, Optional<String> defaultSchema)
    {
        return node.accept(new TNodeRewriter<Void>()
        {
            private final Map<TNode, Set<String>> fieldSetsByNode = new HashMap<>();

            @Override
            public TNode visitSelect(TSelect treeNode, Void context)
            {
                List<TAliasedRelation> relations = addRelationAliases(
                        treeNode.getRelations().stream()
                                .map(r -> (TAliasedRelation) r.accept(this, context))
                                .collect(toImmutableList()));

                List<TSelectItem> items = addItemLabels(
                        treeNode.getItems(),
                        relations,
                        fieldSetsByNode);

                Set<String> fields = items.stream()
                        .map(TExpressionSelectItem.class::cast)
                        .map(TExpressionSelectItem::getLabel)
                        .map(Optional::get)
                        .collect(toImmutableSet());

                TSelect ret = new TSelect(
                        items,
                        relations,
                        treeNode.getWhere().map(w -> (TExpression) w.accept(this, context)));

                fieldSetsByNode.put(ret, fields);

                return ret;
            }

            @Override
            public TNode visitSubqueryRelation(TSubqueryRelation treeNode, Void context)
            {
                return super.visitSubqueryRelation(treeNode, context);
            }

            @Override
            public TNode visitTableName(TTableName treeNode, Void context)
            {
                TTableName ret = (TTableName) super.visitTableName(treeNode, context);
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);
                Table table = catalog.getSchemaTable(schemaTable);
                fieldSetsByNode.put(ret, ImmutableSet.copyOf(table.getRowLayout().getFieldNames()));
                return ret;
            }
        }, null);
    }
}
