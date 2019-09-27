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

import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.tree.AliasedRelation;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SubqueryRelation;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.TraversalVisitor;

import java.util.LinkedHashSet;
import java.util.List;
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
        private final Optional<Catalog> catalog;
        private final Optional<String> defaultSchema;

        public ScopeAstVisitor(Optional<Catalog> catalog, Optional<String> defaultSchema)
        {
            this.catalog = checkNotNull(catalog);
            this.defaultSchema = checkNotNull(defaultSchema);
        }

        @Override
        public Scope visitAliasedRelation(AliasedRelation treeNode, Optional<Scope> context)
        {
            Scope aliasScope = new Scope(treeNode, context, treeNode.getAlias());
            treeNode.getRelation().accept(this, Optional.of(aliasScope));
            return aliasScope;
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
                relation.accept(this, Optional.of(scope));
            });

            treeNode.getItems().forEach(item -> {
                item.accept(this, Optional.of(scope));
            });

            return scope;
        }

        @Override
        public Scope visitSubqueryRelation(SubqueryRelation treeNode, Optional<Scope> context)
        {
            return treeNode.getSelect().accept(this, context);
        }

        @Override
        public Scope visitTableName(TableName treeNode, Optional<Scope> context)
        {
            List<String> tableNameParts = treeNode.getQualifiedName().getParts();
            SchemaTable schemaTable;
            if (tableNameParts.size() == 1) {
                schemaTable = SchemaTable.of(defaultSchema.get(), tableNameParts.get(0));
            }
            else if (tableNameParts.size() == 2) {
                schemaTable = SchemaTable.of(tableNameParts.get(0), tableNameParts.get(1));
            }
            else {
                throw new IllegalArgumentException(tableNameParts.toString());
            }

            Scope scope = new Scope(treeNode, context, Optional.of(schemaTable.getTable()));

            Table table = catalog.get().getSchemaTable(schemaTable);
            table.getRowLayout().getFields().keySet().forEach(f -> new Symbol(Optional.of(f), treeNode, scope));

            return scope;
        }
    }

    public static void analyze(TreeNode statement, Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        Scope scope = statement.accept(new ScopeAstVisitor(catalog, defaultSchema), Optional.empty());
        checkNotNull(scope);
    }
}
