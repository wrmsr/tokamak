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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TAllSelectItem;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TJoinRelation;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TRelation;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableNameRelation;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeRewriter;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeVisitor;
import com.wrmsr.tokamak.core.tree.node.visitor.TraversalTNodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollections.histogram;

public final class SelectExpansion
{
    private SelectExpansion()
    {
    }

    private static List<TRelation> addRelationAliases(List<TRelation> relations)
    {
        Set<String> seen = new HashSet<>();
        List<TAliasedRelation> ret = new ArrayList<>();
        AtomicInteger numAnon = new AtomicInteger(0);

        Map<String, Integer> tableNameCounts = new HashMap<>();
        relations.forEach(r -> r.accept(new TraversalTNodeVisitor<Void, Void>()
        {
            @Override
            public Void visitTableNameRelation(TTableNameRelation node, Void context)
            {
                String name = node.getQualifiedName().getLast();
                tableNameCounts.put(name, tableNameCounts.getOrDefault(name, 0) + 1);
                return null;
            }
        }, null));

        Map<String, Integer> dupeTableNameCounts = new HashMap<>();

        return relations.stream()
                .map(r -> r.accept(new TNodeVisitor<TRelation, Void>()
                {
                    @Override
                    public TRelation visitAliasedRelation(TAliasedRelation node, Void context)
                    {
                        return node;
                    }

                    @Override
                    public TRelation visitJoinRelation(TJoinRelation node, Void context)
                    {
                        return new TJoinRelation(
                                process(node.getLeft(), context),
                                process(node.getRight(), context),
                                node.getCondition());
                    }

                    @Override
                    public TRelation visitSubqueryRelation(TSubqueryRelation node, Void context)
                    {
                        return new TAliasedRelation(node, "_" + numAnon.getAndIncrement());
                    }

                    @Override
                    public TRelation visitTableNameRelation(TTableNameRelation node, Void context)
                    {
                        String name = node.getQualifiedName().getLast();
                        String alias;
                        if (tableNameCounts.get(name) > 1) {
                            int num = dupeTableNameCounts.getOrDefault(name, 0);
                            dupeTableNameCounts.put(name, num + 1);
                            alias = name + "_" + num;
                        }
                        else {
                            alias = name;
                        }
                        return new TAliasedRelation(node, alias);
                    }
                }, null))
                .collect(toImmutableList());
    }

    private static Map<TNode, Set<String>> buildFieldSetsByNode(
            List<TRelation> relations,
            ParsingContext parsingContext)
    {
        Map<TNode, Set<String>> fieldSetsByNode = new HashMap<>();

        relations.forEach(r -> r.accept(new TraversalTNodeVisitor<Void, Void>()
        {
            @Override
            public Void visitTableNameRelation(TTableNameRelation treeNode, Void context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(parsingContext.getDefaultSchema());
                Table table = parsingContext.getCatalog().get().getSchemaTable(schemaTable);
                fieldSetsByNode.put(treeNode, table.getRowLayout().getFields().getNames());
                return null;
            }
        }, null));

        return ImmutableMap.copyOf(fieldSetsByNode);
    }

    private static List<TSelectItem> addItemLabels(
            List<TSelectItem> items,
            List<TRelation> relations,
            Map<TNode, Set<String>> fieldSetsByNode)
    {
        Set<String> seen = new HashSet<>();
        List<TSelectItem> ret = new ArrayList<>();
        int numAnon = 0;

        Map<String, Long> relationFieldCounts = histogram(fieldSetsByNode.values().stream().flatMap(Set::stream));

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
                                                        ImmutableList.of(relation.getAlias(), relationField))),
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

    public static TNode expandSelects(TNode node, ParsingContext parsingContext)
    {
        return node.accept(new TNodeRewriter<Void>()
        {
            private final Map<TNode, Set<String>> fieldSetsByNode = new HashMap<>();

            @Override
            public TNode visitSelect(TSelect treeNode, Void context)
            {
                List<TAliasedRelation> relations = addRelationAliases(
                        treeNode.getRelations().stream()
                                .map(r -> (TRelation) process(r, context))
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
                        relations.stream().map(TAliasedRelation.class::cast).collect(toImmutableList()),
                        treeNode.getWhere().map(w -> (TExpression) process(w, context)));

                fieldSetsByNode.put(ret, fields);

                return ret;
            }

            @Override
            public TNode visitJoinRelation(TJoinRelation node, Void context)
            {
                return super.visitJoinRelation(node, context);
            }

            @Override
            public TNode visitSubqueryRelation(TSubqueryRelation treeNode, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public TNode visitTableNameRelation(TTableNameRelation treeNode, Void context)
            {
                TTableNameRelation ret = (TTableNameRelation) super.visitTableNameRelation(treeNode, context);
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(parsingContext.getDefaultSchema());
                Table table = parsingContext.getCatalog().get().getSchemaTable(schemaTable);
                fieldSetsByNode.put(ret, table.getRowLayout().getFields().getNames());
                return ret;
            }
        }, null);
    }
}
