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
package com.wrmsr.tokamak.core.parse.analysis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.QualifiedNameExpression;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.TraversalVisitor;
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

public final class ScopeAnalysis
{
    public static final class Symbol
    {
        private final Optional<String> name;
        private final Scope scope;
        private final Optional<Symbol> origin;
        private final TreeNode node;

        public Symbol(Optional<String> name, TreeNode node, Optional<Symbol> origin, Scope scope)
        {
            this.name = checkNotNull(name);
            this.scope = checkNotNull(scope);
            this.origin = checkNotNull(origin);
            this.node = checkNotNull(node);

            scope.symbols.add(this);
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

        public Scope getScope()
        {
            return scope;
        }

        public Optional<Symbol> getOrigin()
        {
            return origin;
        }

        public TreeNode getNode()
        {
            return node;
        }
    }

    public static final class SymbolRef
    {
        private final Optional<List<String>> nameParts;
        private final TreeNode node;
        private final Optional<Symbol> binding;
        private final Scope scope;

        public SymbolRef(Optional<List<String>> nameParts, TreeNode node, Optional<Symbol> binding, Scope scope)
        {
            this.nameParts = checkNotNull(nameParts).map(ImmutableList::copyOf);
            this.node = checkNotNull(node);
            this.binding = checkNotNull(binding);
            this.scope = checkNotNull(scope);

            scope.symbolRefs.add(this);
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

        public TreeNode getNode()
        {
            return node;
        }

        public Optional<Symbol> getBinding()
        {
            return binding;
        }

        public Scope getScope()
        {
            return scope;
        }
    }

    public static final class Scope
    {
        private final TreeNode node;
        private final Set<TreeNode> enclosedNodes = new LinkedHashSet<>();

        private final Optional<Scope> parent;
        private final Set<Scope> children = new LinkedHashSet<>();

        private final Optional<String> name;

        private final Set<Symbol> symbols = new LinkedHashSet<>();
        private final Set<SymbolRef> symbolRefs = new LinkedHashSet<>();

        public Scope(TreeNode node, Optional<Scope> parent, Optional<String> name)
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
                    '}';
        }

        public TreeNode getNode()
        {
            return node;
        }

        public Set<TreeNode> getEnclosedNodes()
        {
            return Collections.unmodifiableSet(enclosedNodes);
        }

        public Optional<Scope> getParent()
        {
            return parent;
        }

        public Set<Scope> getChildren()
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

    private final Scope rootScope;

    private final Set<Scope> scopes;
    private final Map<TreeNode, Scope> scopesByNode;
    private final Map<TreeNode, Set<Symbol>> symbolSetsByNode;
    private final Map<TreeNode, SymbolRef> symbolRefsByNode;

    private ScopeAnalysis(Scope rootScope)
    {
        this.rootScope = checkNotNull(rootScope);

        ImmutableMap.Builder<TreeNode, Scope> scopesByNode = ImmutableMap.builder();
        Map<TreeNode, Set<Symbol>> symbolSetsByNode = new LinkedHashMap<>();
        ImmutableMap.Builder<TreeNode, SymbolRef> symbolRefsByNode = ImmutableMap.builder();

        Set<Scope> seen = new HashSet<>();
        Queue<Scope> queue = new ArrayDeque<>();
        queue.add(rootScope);
        seen.add(rootScope);
        while (!queue.isEmpty()) {
            Scope cur = queue.remove();
            cur.enclosedNodes.forEach(n -> scopesByNode.put(n, cur));
            cur.symbols.forEach(s -> symbolSetsByNode.computeIfAbsent(s.node, n -> new LinkedHashSet<>()).add(s));
            cur.symbolRefs.forEach(sr -> symbolRefsByNode.put(sr.node, sr));
            cur.children.forEach(c -> {
                checkState(!seen.contains(c));
                seen.add(c);
                queue.add(c);
            });
        }

        this.scopes = ImmutableSet.copyOf(seen);
        this.scopesByNode = scopesByNode.build();
        this.symbolSetsByNode = newImmutableSetMap(symbolSetsByNode);
        this.symbolRefsByNode = symbolRefsByNode.build();
    }

    public static List<Symbol> getScopeMatches(SymbolRef symbolRef)
    {
        checkArgument(symbolRef.nameParts.isPresent());
        return symbolRef.getScope().getChildren().stream()
                .map(ScopeAnalysis.Scope::getSymbols)
                .flatMap(Set::stream)
                .filter(s -> optionalTest(s.getName(), symbolRef.nameParts.get().get(0)::equals))
                .collect(toImmutableList());
    }

    public static List<Symbol> getSymbolMatches(SymbolRef symbolRef)
    {
        checkArgument(optionalTest(symbolRef.nameParts, np -> np.size() > 1));
        return symbolRef.getScope().getChildren().stream()
                .filter(s -> optionalTest(s.getName(), symbolRef.nameParts.get().get(0)::equals))
                .map(ScopeAnalysis.Scope::getSymbols)
                .flatMap(Set::stream)
                .filter(s -> optionalTest(s.getName(), symbolRef.nameParts.get().get(1)::equals))
                .collect(toImmutableList());
    }

    public Set<Scope> getScopes()
    {
        return scopes;
    }

    public Map<TreeNode, Scope> getScopesByNode()
    {
        return scopesByNode;
    }

    public Optional<Scope> getScope(TreeNode node)
    {
        return Optional.ofNullable(scopesByNode.get(node));
    }

    public Map<TreeNode, Set<Symbol>> getSymbolSetsByNode()
    {
        return symbolSetsByNode;
    }

    public Map<TreeNode, SymbolRef> getSymbolRefsByNode()
    {
        return symbolRefsByNode;
    }

    private final SupplierLazyValue<Resolutions> resolutions = new SupplierLazyValue<>();

    public Resolutions getResolutions()
    {
        return resolutions.get(() -> {
            ImmutableMap.Builder<SymbolRef, Symbol> symbolResolutions = ImmutableMap.builder();
            Map<Symbol, Set<SymbolRef>> symbolRefResolutions = new LinkedHashMap<>();

            scopes.forEach(cur -> {
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

    public static ScopeAnalysis analyze(TreeNode statement, Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        Scope scope = statement.accept(new TraversalVisitor<Scope, Scope>()
        {
            @Override
            protected Scope visitTreeNode(TreeNode treeNode, Scope context)
            {
                if (context != null) {
                    context.enclosedNodes.add(treeNode);
                }
                return null;
            }

            @Override
            public Scope visitAllSelectItem(AllSelectItem treeNode, Scope context)
            {
                context.enclosedNodes.add(treeNode);
                context.children.forEach(c -> c.symbols.forEach(s -> {
                    Symbol symbol = new Symbol(s.name, treeNode, Optional.of(s), context);
                    new SymbolRef(s.name.map(ImmutableList::of), treeNode, Optional.of(symbol), context);
                }));
                return null;
            }

            @Override
            public Scope visitExpressionSelectItem(ExpressionSelectItem treeNode, Scope context)
            {
                context.enclosedNodes.add(treeNode);
                treeNode.getExpression().accept(this, context);
                Optional<String> label = treeNode.getLabel();
                if (!label.isPresent() && treeNode.getExpression() instanceof QualifiedNameExpression) {
                    label = Optional.of(((QualifiedNameExpression) treeNode.getExpression()).getQualifiedName().getLast());
                }
                new Symbol(label, treeNode, Optional.empty(), context);
                return null;
            }

            @Override
            public Scope visitQualifiedNameExpression(QualifiedNameExpression treeNode, Scope context)
            {
                context.enclosedNodes.add(treeNode);
                new SymbolRef(Optional.of(treeNode.getQualifiedName().getParts()), treeNode, Optional.empty(), context);
                return null;
            }

            @Override
            public Scope visitSelect(Select treeNode, Scope context)
            {
                Scope scope = new Scope(treeNode, Optional.ofNullable(context), Optional.empty());

                treeNode.getRelations().forEach(aliasedRelation -> {
                    Scope relationScope = new Scope(aliasedRelation, Optional.of(scope), aliasedRelation.getAlias());
                    aliasedRelation.getRelation().accept(this, relationScope);
                });

                treeNode.getWhere().ifPresent(where -> where.accept(this, scope));
                treeNode.getItems().forEach(item -> item.accept(this, scope));

                return scope;
            }

            @Override
            public Scope visitSubqueryRelation(SubqueryRelation treeNode, Scope context)
            {
                context.enclosedNodes.add(treeNode);
                return treeNode.getSelect().accept(this, context);
            }

            @Override
            public Scope visitTableName(TableName treeNode, Scope context)
            {
                context.enclosedNodes.add(treeNode);
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);
                Table table = catalog.get().getSchemaTable(schemaTable);
                table.getRowLayout().getFields().getNames().forEach(f -> new Symbol(Optional.of(f), treeNode, Optional.empty(), context));
                return null;
            }
        }, null);

        return new ScopeAnalysis(scope);
    }
}
