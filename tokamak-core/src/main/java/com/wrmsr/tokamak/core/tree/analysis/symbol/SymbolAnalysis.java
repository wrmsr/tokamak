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
package com.wrmsr.tokamak.core.tree.analysis.symbol;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TAllSelectItem;
import com.wrmsr.tokamak.core.tree.node.TBooleanExpression;
import com.wrmsr.tokamak.core.tree.node.TComparisonExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TJoinRelation;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TNotExpression;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableNameRelation;
import com.wrmsr.tokamak.core.tree.node.visitor.TraversalTNodeVisitor;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;
import static com.wrmsr.tokamak.util.MoreOptionals.optionalTest;

public final class SymbolAnalysis
{
    final SymbolScope rootSymbolScope;

    final Set<SymbolScope> symbolScopes;
    final Map<TNode, SymbolScope> symbolScopesByNode;
    final Map<TNode, Set<Symbol>> symbolSetsByNode;
    final Map<TNode, SymbolRef> symbolRefsByNode;

    private SymbolAnalysis(SymbolScope rootSymbolScope)
    {
        this.rootSymbolScope = checkNotNull(rootSymbolScope);

        ImmutableMap.Builder<TNode, SymbolScope> symbolScopesByNode = ImmutableMap.builder();
        Map<TNode, Set<Symbol>> symbolSetsByNode = new LinkedHashMap<>();
        ImmutableMap.Builder<TNode, SymbolRef> symbolRefsByNode = ImmutableMap.builder();

        Set<SymbolScope> seen = new HashSet<>();
        Queue<SymbolScope> queue = new ArrayDeque<>();
        queue.add(rootSymbolScope);
        seen.add(rootSymbolScope);
        while (!queue.isEmpty()) {
            SymbolScope cur = queue.remove();
            cur.enclosedNodes.forEach(n -> symbolScopesByNode.put(n, cur));
            cur.symbols.forEach(s -> symbolSetsByNode.computeIfAbsent(s.node, n -> new LinkedHashSet<>()).add(s));
            cur.symbolRefs.forEach(sr -> symbolRefsByNode.put(sr.node, sr));
            cur.children.forEach(c -> {
                checkState(!seen.contains(c));
                seen.add(c);
                queue.add(c);
            });
        }

        this.symbolScopes = ImmutableSet.copyOf(seen);
        this.symbolScopesByNode = symbolScopesByNode.build();
        this.symbolSetsByNode = newImmutableSetMap(symbolSetsByNode);
        this.symbolRefsByNode = symbolRefsByNode.build();
    }

    public static List<Symbol> getSymbolScopeMatches(SymbolRef symbolRef)
    {
        checkArgument(symbolRef.nameParts.isPresent());
        return symbolRef.getSymbolScope().getChildren().stream()
                .map(SymbolScope::getSymbols)
                .flatMap(Set::stream)
                .filter(s -> optionalTest(s.getName(), symbolRef.nameParts.get().get(0)::equals))
                .collect(toImmutableList());
    }

    public static List<Symbol> getSymbolMatches(SymbolRef symbolRef)
    {
        checkArgument(optionalTest(symbolRef.nameParts, np -> np.size() > 1));
        return symbolRef.getSymbolScope().getChildren().stream()
                .filter(s -> optionalTest(s.getName(), symbolRef.nameParts.get().get(0)::equals))
                .map(SymbolScope::getSymbols)
                .flatMap(Set::stream)
                .filter(s -> optionalTest(s.getName(), symbolRef.nameParts.get().get(1)::equals))
                .collect(toImmutableList());
    }

    public Set<SymbolScope> getSymbolScopes()
    {
        return symbolScopes;
    }

    public Map<TNode, SymbolScope> getSymbolScopesByNode()
    {
        return symbolScopesByNode;
    }

    public Optional<SymbolScope> getSymbolScope(TNode node)
    {
        return Optional.ofNullable(symbolScopesByNode.get(node));
    }

    public Map<TNode, Set<Symbol>> getSymbolSetsByNode()
    {
        return symbolSetsByNode;
    }

    public Map<TNode, SymbolRef> getSymbolRefsByNode()
    {
        return symbolRefsByNode;
    }

    private final SupplierLazyValue<SymbolResolutions> resolutions = new SupplierLazyValue<>();

    public SymbolResolutions getResolutions()
    {
        return resolutions.get(() -> {
            ImmutableMap.Builder<SymbolRef, Symbol> symbolResolutions = ImmutableMap.builder();
            Map<Symbol, Set<SymbolRef>> symbolRefResolutions = new LinkedHashMap<>();

            symbolScopes.forEach(cur -> {
                cur.symbolRefs.forEach(sr -> {
                    sr.nameParts.ifPresent(parts -> {
                        if (parts.size() > 1) {
                            List<Symbol> hits = getSymbolMatches(sr);
                            if (hits.size() == 1) {
                                Symbol s = hits.get(0);
                                symbolResolutions.put(sr, s);
                                symbolRefResolutions.computeIfAbsent(s, s_ -> new LinkedHashSet<>()).add(sr);
                            }
                        }
                    });
                });
            });

            return new SymbolResolutions(
                    symbolResolutions.build(),
                    symbolRefResolutions);
        });
    }

    public static SymbolAnalysis analyze(TNode statement, ParsingContext parsingContext)
    {
        SymbolScope symbolScope = statement.accept(new TraversalTNodeVisitor<SymbolScope, SymbolScope>()
        {
            @Override
            protected SymbolScope visitNode(TNode treeNode, SymbolScope context)
            {
                if (context != null) {
                    context.enclosedNodes.add(treeNode);
                }
                return null;
            }

            @Override
            public SymbolScope visitAliasedRelation(TAliasedRelation node, SymbolScope context)
            {
                SymbolScope relationSymbolScope = new SymbolScope(node, Optional.of(context), Optional.of(node.getAlias()));
                process(node.getRelation(), relationSymbolScope);
                return null;
            }

            @Override
            public SymbolScope visitAllSelectItem(TAllSelectItem treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                context.children.forEach(c -> c.symbols.forEach(s -> {
                    Symbol symbol = new Symbol(s.name, treeNode, Optional.of(s), context);
                    new SymbolRef(s.name.map(ImmutableList::of), treeNode, Optional.of(symbol), context);
                }));
                return null;
            }

            @Override
            public SymbolScope visitBooleanExpression(TBooleanExpression treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                process(treeNode.getLeft(), context);
                process(treeNode.getRight(), context);
                return null;
            }

            @Override
            public SymbolScope visitComparisonExpression(TComparisonExpression treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                process(treeNode.getLeft(), context);
                process(treeNode.getRight(), context);
                return null;
            }

            @Override
            public SymbolScope visitExpressionSelectItem(TExpressionSelectItem treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                process(treeNode.getExpression(), context);
                Optional<String> label = treeNode.getLabel();
                if (!label.isPresent() && treeNode.getExpression() instanceof TQualifiedNameExpression) {
                    label = Optional.of(((TQualifiedNameExpression) treeNode.getExpression()).getQualifiedName().getLast());
                }
                new Symbol(label, treeNode, Optional.empty(), context);
                return null;
            }

            @Override
            public SymbolScope visitJoinRelation(TJoinRelation node, SymbolScope context)
            {
                throw new IllegalStateException();
            }

            @Override
            public SymbolScope visitNotExpression(TNotExpression treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                process(treeNode.getExpression(), context);
                return null;
            }

            @Override
            public SymbolScope visitQualifiedNameExpression(TQualifiedNameExpression treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                new SymbolRef(Optional.of(treeNode.getQualifiedName().getParts()), treeNode, Optional.empty(), context);
                return null;
            }

            @Override
            public SymbolScope visitSelect(TSelect treeNode, SymbolScope context)
            {
                SymbolScope symbolScope = new SymbolScope(treeNode, Optional.ofNullable(context), Optional.empty());

                treeNode.getRelations().forEach(relation -> process(relation, symbolScope));
                treeNode.getWhere().ifPresent(where -> process(where, symbolScope));
                treeNode.getItems().forEach(item -> process(item, symbolScope));

                return symbolScope;
            }

            @Override
            public SymbolScope visitSubqueryRelation(TSubqueryRelation treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                return process(treeNode.getSelect(), context);
            }

            @Override
            public SymbolScope visitTableNameRelation(TTableNameRelation treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(parsingContext.getDefaultSchema());
                Table table = parsingContext.getCatalog().get().getSchemaTable(schemaTable);
                table.getRowLayout().getFields().getNames().forEach(f -> new Symbol(Optional.of(f), treeNode, Optional.empty(), context));
                return null;
            }
        }, null);

        return new SymbolAnalysis(symbolScope);
    }
}
