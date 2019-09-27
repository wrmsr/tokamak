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
package com.wrmsr.tokamak.core.parse;

import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.TraversalVisitor;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AstAnalysis
{
    private AstAnalysis()
    {
    }

    public static final class Symbol
    {
        private final Optional<String> name;
        private final Scope scope;
        private final TreeNode node;

        public Symbol(Optional<String> name, TreeNode node, Scope scope)
        {
            this.name = checkNotNull(name);
            this.scope = checkNotNull(scope);
            this.node = checkNotNull(node);

            scope.symbols.add(this);
        }
    }

    public static final class SymbolRef
    {
        private final QualifiedName qualifiedName;
        private final Scope scope;

        public SymbolRef(QualifiedName qualifiedName, Scope scope)
        {
            this.qualifiedName = checkNotNull(qualifiedName);
            this.scope = checkNotNull(scope);

            scope.symbolRefs.add(this);
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
    }

    public static final class ScopeAstVisitor
            extends TraversalVisitor<Scope, Optional<Scope>>
    {
        private final Catalog catalog;

        public ScopeAstVisitor(Catalog catalog)
        {
            this.catalog = checkNotNull(catalog);
        }

        @Override
        public Scope visitExpressionSelectItem(ExpressionSelectItem treeNode, Optional<Scope> context)
        {
            treeNode.getExpression().accept(this, context);

            new Symbol(treeNode.getLabel(), treeNode, context.get());

            return null;
        }

        @Override
        public Scope visitQualifiedName(QualifiedName treeNode, Optional<Scope> context)
        {
            new SymbolRef(treeNode, context.get());

            return null;
        }

        @Override
        public Scope visitSelect(Select treeNode, Optional<Scope> context)
        {
            Scope scope = new Scope(treeNode, context, Optional.empty());

            treeNode.getRelations().forEach(relation -> {
                Scope relationScope = relation.accept(this, Optional.of(scope));
                if (relationScope != null) {

                }
            });

            treeNode.getItems().forEach(item -> {

                item.accept(this, Optional.of(scope));
            });

            return scope;
        }
    }

    public static void analyze(TreeNode statement, Catalog catalog)
    {
        statement.accept(new ScopeAstVisitor(catalog), Optional.empty());
    }
}
