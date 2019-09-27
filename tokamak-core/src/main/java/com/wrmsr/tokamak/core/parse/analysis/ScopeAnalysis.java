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
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.tree.AliasedRelation;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.TraversalVisitor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

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

        public Optional<Scope> getParent()
        {
            return parent;
        }

        public Set<Scope> getChildren()
        {
            return children;
        }

        public Optional<String> getName()
        {
            return name;
        }

        public Set<Symbol> getSymbols()
        {
            return symbols;
        }

        public Set<SymbolRef> getSymbolRefs()
        {
            return symbolRefs;
        }
    }

    private final Map<TreeNode, Scope> scopesByNode;

    private ScopeAnalysis(Map<TreeNode, Scope> scopesByNode)
    {
        this.scopesByNode = ImmutableMap.copyOf(scopesByNode);
    }

    public Map<TreeNode, Scope> getScopesByNode()
    {
        return scopesByNode;
    }

    public static ScopeAnalysis analyze(TreeNode statement, Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        Map<TreeNode, Scope> scopesByNode = new HashMap<>();

        statement.accept(new TraversalVisitor<Scope, Scope>()
        {
            @Override
            public Scope visitAliasedRelation(AliasedRelation treeNode, Scope context)
            {
                throw new IllegalStateException();
            }

            @Override
            public Scope visitAllSelectItem(AllSelectItem treeNode, Scope context)
            {
                context.children.forEach(c -> c.symbols.forEach(s -> {
                    Symbol symbol = new Symbol(s.name, treeNode, Optional.of(s), context);
                    new SymbolRef(s.name.map(ImmutableList::of), treeNode, Optional.of(symbol), context);
                }));
                return null;
            }

            @Override
            public Scope visitExpressionSelectItem(ExpressionSelectItem treeNode, Scope context)
            {
                treeNode.getExpression().accept(this, context);
                Optional<String> label = treeNode.getLabel();
                if (!label.isPresent() && treeNode.getExpression() instanceof QualifiedName) {
                    label = Optional.of(((QualifiedName) treeNode.getExpression()).getLast());
                }
                new Symbol(label, treeNode, Optional.empty(), context);
                return null;
            }

            @Override
            public Scope visitQualifiedName(QualifiedName treeNode, Scope context)
            {
                new SymbolRef(Optional.of(treeNode.getParts()), treeNode, Optional.empty(), context);
                return null;
            }

            @Override
            public Scope visitSelect(Select treeNode, Scope context)
            {
                Scope scope = new Scope(treeNode, Optional.ofNullable(context), Optional.empty());
                scopesByNode.put(treeNode, scope);

                treeNode.getRelations().forEach(aliasedRelation -> {
                    Scope relationScope = new Scope(aliasedRelation, Optional.of(scope), aliasedRelation.getAlias());
                    aliasedRelation.getRelation().accept(this, relationScope);
                });

                treeNode.getWhere().ifPresent(where -> {
                    where.accept(this, scope);
                });

                treeNode.getItems().forEach(item -> {
                    item.accept(this, scope);
                });

                return scope;
            }

            @Override
            public Scope visitSubqueryRelation(SubqueryRelation treeNode, Scope context)
            {
                return treeNode.getSelect().accept(this, context);
            }

            @Override
            public Scope visitTableName(TableName treeNode, Scope context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);
                Table table = catalog.get().getSchemaTable(schemaTable);
                table.getRowLayout().getFields().keySet().forEach(f -> new Symbol(Optional.of(f), treeNode, Optional.empty(), context));
                return null;
            }
        }, null);

        return new ScopeAnalysis(scopesByNode);
    }
}
