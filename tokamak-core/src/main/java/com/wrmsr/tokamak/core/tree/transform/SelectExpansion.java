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
import com.wrmsr.tokamak.core.tree.node.visitor.CachingTNodeVisitor;
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
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapOfSame;
import static java.util.stream.Collectors.groupingBy;

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

    private static Map<TNode, Map<String, Integer>> buildFieldHistogramsByNode(
            List<TRelation> relations,
            ParsingContext parsingContext)
    {
        Map<TNode, Map<String, Integer>> ret = new HashMap<>();

        relations.forEach(r -> new CachingTNodeVisitor<Map<String, Integer>, Void>(ret)
        {
            @Override
            public Map<String, Integer> visitAliasedRelation(TAliasedRelation node, Void context)
            {
                return process(node.getRelation(), context);
            }

            @Override
            public Map<String, Integer> visitJoinRelation(TJoinRelation node, Void context)
            {
                Map<String, Integer> ret = new HashMap<>();
                Stream.of(node.getLeft(), node.getRight())
                        .forEach(r -> process(r, context).forEach((f, c) -> ret.put(f, ret.getOrDefault(f, 0) + 1)));
                return ret;
            }

            @Override
            public Map<String, Integer> visitSubqueryRelation(TSubqueryRelation node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public Map<String, Integer> visitTableNameRelation(TTableNameRelation treeNode, Void context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(parsingContext.getDefaultSchema());
                Table table = parsingContext.getCatalog().get().getSchemaTable(schemaTable);
                return immutableMapOfSame(table.getRowLayout().getFields().getNames(), 1);
            }
        }.process(r, null));

        return ImmutableMap.copyOf(ret);
    }

    private static List<TSelectItem> addItemLabels(
            List<TSelectItem> items,
            List<TRelation> relations,
            ParsingContext parsingContext)
    {
        Map<TNode, Map<String, Integer>> fieldHistogramsByNode = buildFieldHistogramsByNode(relations, parsingContext);
        Set<String> uniqueFields = relations.stream()
                .map(fieldHistogramsByNode::get)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(groupingBy(Map.Entry::getKey)).entrySet().stream()
                .filter(e -> e.getValue().size() == 1 && e.getValue().get(0).getValue() == 1)
                .map(Map.Entry::getKey)
                .collect(toImmutableSet());

        Set<String> seen = new HashSet<>();
        List<TSelectItem> ret = new ArrayList<>();
        int numAnon = 0;

        for (TSelectItem item : items) {
            if (item instanceof TAllSelectItem) {
                Map<String, Integer> dupeCounts = new HashMap<>();
                for (TRelation relation : relations) {
                    relation.accept(new TNodeVisitor<Void, Optional<String>>()
                    {
                        @Override
                        public Void visitAliasedRelation(TAliasedRelation node, Optional<String> context)
                        {
                            process(node.getRelation(), Optional.of(node.getAlias()));
                            return null;
                        }

                        @Override
                        public Void visitJoinRelation(TJoinRelation node, Optional<String> context)
                        {
                            process(node.getLeft(), context);
                            process(node.getRight(), context);
                            return null;
                        }

                        @Override
                        public Void visitSubqueryRelation(TSubqueryRelation node, Optional<String> context)
                        {
                            throw new IllegalStateException();
                        }

                        @Override
                        public Void visitTableNameRelation(TTableNameRelation node, Optional<String> context)
                        {
                            Set<String> relationFields = fieldHistogramsByNode.get(node).keySet();
                            for (String relationField : relationFields) {
                                String label;
                                if (!uniqueFields.contains(relationField)) {
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
                                                                ImmutableList.of(context.get(), relationField))),
                                                Optional.of(label)));
                            }
                            return null;
                        }
                    }, Optional.empty());
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
                List<TRelation> relations = addRelationAliases(
                        treeNode.getRelations().stream()
                                .map(r -> (TRelation) process(r, context))
                                .collect(toImmutableList()));

                List<TSelectItem> items = addItemLabels(
                        treeNode.getItems(),
                        relations,
                        parsingContext);

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
        }, null);
    }
}