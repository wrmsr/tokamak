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
package com.wrmsr.tokamak.core.tree.analysis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.tree.node.TAllSelectItem;
import com.wrmsr.tokamak.core.tree.node.TComparisonExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableName;
import com.wrmsr.tokamak.core.tree.node.visitor.TraversalTNodeVisitor;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.util.ArrayDeque;
import java.util.Collections;
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
    public static final class Symbol
    {
        private final Optional<String> name;
        private final SymbolScope symbolScope;
        private final Optional<Symbol> origin;
        private final TNode node;

        public Symbol(Optional<String> name, TNode node, Optional<Symbol> origin, SymbolScope symbolScope)
        {
            this.name = checkNotNull(name);
            this.symbolScope = checkNotNull(symbolScope);
            this.origin = checkNotNull(origin);
            this.node = checkNotNull(node);

            symbolScope.symbols.add(this);
        }

        @Override
        public String toString()
        {
            return "Symbol{" +
                    "name=" + name +
                    '}';
        }

        public Optional<String> getName()
        {
            return name;
        }

        public SymbolScope getSymbolScope()
        {
            return symbolScope;
        }

        public Optional<Symbol> getOrigin()
        {
            return origin;
        }

        public TNode getNode()
        {
            return node;
        }
    }

    public static final class SymbolRef
    {
        private final Optional<List<String>> nameParts;
        private final TNode node;
        private final Optional<Symbol> binding;
        private final SymbolScope symbolScope;

        public SymbolRef(Optional<List<String>> nameParts, TNode node, Optional<Symbol> binding, SymbolScope symbolScope)
        {
            this.nameParts = checkNotNull(nameParts).map(ImmutableList::copyOf);
            this.node = checkNotNull(node);
            this.binding = checkNotNull(binding);
            this.symbolScope = checkNotNull(symbolScope);

            symbolScope.symbolRefs.add(this);
        }

        @Override
        public String toString()
        {
            return "SymbolRef{" +
                    "nameParts=" + nameParts +
                    '}';
        }

        public Optional<List<String>> getNameParts()
        {
            return nameParts;
        }

        public TNode getNode()
        {
            return node;
        }

        public Optional<Symbol> getBinding()
        {
            return binding;
        }

        public SymbolScope getSymbolScope()
        {
            return symbolScope;
        }
    }

    public static final class SymbolScope
    {
        private final TNode node;
        private final Set<TNode> enclosedNodes = new LinkedHashSet<>();

        private final Optional<SymbolScope> parent;
        private final Set<SymbolScope> children = new LinkedHashSet<>();

        private final Optional<String> name;

        private final Set<Symbol> symbols = new LinkedHashSet<>();
        private final Set<SymbolRef> symbolRefs = new LinkedHashSet<>();

        public SymbolScope(TNode node, Optional<SymbolScope> parent, Optional<String> name)
        {
            this.node = checkNotNull(node);
            this.parent = checkNotNull(parent);
            this.name = checkNotNull(name);

            parent.ifPresent(p -> p.children.add(this));
            enclosedNodes.add(node);
        }

        @Override
        public String toString()
        {
            return "Scope{" +
                    "node=" + node +
                    ", name=" + name +
                    '}';
        }

        public TNode getNode()
        {
            return node;
        }

        public Set<TNode> getEnclosedNodes()
        {
            return Collections.unmodifiableSet(enclosedNodes);
        }

        public Optional<SymbolScope> getParent()
        {
            return parent;
        }

        public Set<SymbolScope> getChildren()
        {
            return Collections.unmodifiableSet(children);
        }

        public Optional<String> getName()
        {
            return name;
        }

        public Set<Symbol> getSymbols()
        {
            return Collections.unmodifiableSet(symbols);
        }

        public Set<SymbolRef> getSymbolRefs()
        {
            return Collections.unmodifiableSet(symbolRefs);
        }
    }

    public static final class Resolutions
    {
        private final Map<SymbolRef, Symbol> symbols;
        private final Map<Symbol, Set<SymbolRef>> symbolRefs;

        public Resolutions(Map<SymbolRef, Symbol> symbols, Map<Symbol, Set<SymbolRef>> symbolRefs)
        {
            this.symbols = ImmutableMap.copyOf(symbols);
            this.symbolRefs = newImmutableSetMap(symbolRefs);
        }

        public Map<SymbolRef, Symbol> getSymbols()
        {
            return symbols;
        }

        public Map<Symbol, Set<SymbolRef>> getSymbolRefs()
        {
            return symbolRefs;
        }
    }

    private final SymbolScope rootSymbolScope;

    private final Set<SymbolScope> symbolScopes;
    private final Map<TNode, SymbolScope> symbolScopesByNode;
    private final Map<TNode, Set<Symbol>> symbolSetsByNode;
    private final Map<TNode, SymbolRef> symbolRefsByNode;

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

    private final SupplierLazyValue<Resolutions> resolutions = new SupplierLazyValue<>();

    public Resolutions getResolutions()
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

            return new Resolutions(
                    symbolResolutions.build(),
                    symbolRefResolutions);
        });
    }

    public static SymbolAnalysis analyze(TNode statement, Optional<Catalog> catalog, Optional<String> defaultSchema)
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

                treeNode.getRelations().forEach(aliasedRelation -> {
                    SymbolScope relationSymbolScope = new SymbolScope(aliasedRelation, Optional.of(symbolScope), aliasedRelation.getAlias());
                    process(aliasedRelation.getRelation(), relationSymbolScope);
                });

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
            public SymbolScope visitTableName(TTableName treeNode, SymbolScope context)
            {
                context.enclosedNodes.add(treeNode);
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);
                Table table = catalog.get().getSchemaTable(schemaTable);
                table.getRowLayout().getFields().getNames().forEach(f -> new Symbol(Optional.of(f), treeNode, Optional.empty(), context));
                return null;
            }
        }, null);

        return new SymbolAnalysis(symbolScope);
    }
}
