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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TTableName;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.visitor.TraversalTNodeVisitor;
import com.wrmsr.tokamak.core.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toOptionalSingle;
import static com.wrmsr.tokamak.util.MoreOptionals.optionalTest;

public final class TypeAnalysis
{
    private final Map<TNode, Type> typesByNode;

    private TypeAnalysis(Map<TNode, Type> typesByNode)
    {
        this.typesByNode = ImmutableMap.copyOf(typesByNode);
    }

    public Map<TNode, Type> getTypesByNode()
    {
        return typesByNode;
    }

    public static TypeAnalysis analyze(TNode root, Catalog catalog, Optional<String> defaultSchema)
    {
        SymbolAnalysis symbolAnalysis = SymbolAnalysis.analyze(root, Optional.of(catalog), defaultSchema);
        symbolAnalysis.getResolutions();

        Map<SymbolAnalysis.Symbol, Type> typesBySymbol = new LinkedHashMap<>();
        Map<TNode, Type> typesByNode = new LinkedHashMap<>();

        root.accept(new TraversalTNodeVisitor<Void, Void>()
        {
            @Override
            public Void visitQualifiedNameExpression(TQualifiedNameExpression treeNode, Void context)
            {
                SymbolAnalysis.SymbolRef symbolRef = checkNotNull(symbolAnalysis.getSymbolRefsByNode().get(treeNode));
                SymbolAnalysis.Symbol symbol = symbolAnalysis.getResolutions().getSymbols().get(symbolRef);
                if (symbol != null) {
                    checkNotNull(symbol);
                    Type type = typesBySymbol.get(symbol);
                    if (type != null) {
                        typesByNode.put(treeNode, type);
                    }
                }
                return super.visitQualifiedNameExpression(treeNode, context);
            }

            @Override
            public Void visitTableName(TTableName treeNode, Void context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);
                Table table = catalog.getSchemaTable(schemaTable);
                SymbolAnalysis.Scope scope = symbolAnalysis.getScope(treeNode).get();
                table.getRowLayout().getFields().getTypesByName().forEach((f, t) ->
                        scope.getSymbols().stream()
                                .filter(s -> optionalTest(s.getName(), f::equals))
                                .collect(toOptionalSingle())
                                .ifPresent(s -> typesBySymbol.put(s, t)));
                return null;
            }
        }, null);

        return new TypeAnalysis(typesByNode);
    }
}
