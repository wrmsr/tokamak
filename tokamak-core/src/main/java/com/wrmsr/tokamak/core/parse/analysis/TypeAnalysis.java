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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.tree.QualifiedNameExpression;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.TraversalVisitor;
import com.wrmsr.tokamak.core.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeAnalysis
{
    private final Map<TreeNode, Type> typesByNode;

    private TypeAnalysis(Map<TreeNode, Type> typesByNode)
    {
        this.typesByNode = ImmutableMap.copyOf(typesByNode);
    }

    public Map<TreeNode, Type> getTypesByNode()
    {
        return typesByNode;
    }

    public static TypeAnalysis analyze(TreeNode root, Catalog catalog, Optional<String> defaultSchema)
    {
        ScopeAnalysis scopeAnalysis = ScopeAnalysis.analyze(root, Optional.of(catalog), defaultSchema);
        scopeAnalysis.getResolutions();

        Map<ScopeAnalysis.Symbol, Type> typesBySymbol = new LinkedHashMap<>();
        Map<TreeNode, Type> typesByNode = new LinkedHashMap<>();

        root.accept(new TraversalVisitor<Void, Void>()
        {
            @Override
            public Void visitQualifiedNameExpression(QualifiedNameExpression treeNode, Void context)
            {
                ScopeAnalysis.SymbolRef symbolRef = checkNotNull(scopeAnalysis.getSymbolRefsByNode().get(treeNode));
                ScopeAnalysis.Symbol symbol = scopeAnalysis.getResolutions().getSymbols().get(symbolRef);
                if (symbol != null) {
                    checkNotNull(symbol);
                }
                return super.visitQualifiedNameExpression(treeNode, context);
            }

            @Override
            public Void visitTableName(TableName treeNode, Void context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);
                Table table = catalog.getSchemaTable(schemaTable);
                table.getRowLayout().getFields().keySet().forEach(f -> typesBySymbol
            }
        }, null);

        return new TypeAnalysis(typesByNode);
    }
}
