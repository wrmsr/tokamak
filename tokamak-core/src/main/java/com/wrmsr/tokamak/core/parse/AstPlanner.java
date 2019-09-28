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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.analysis.ScopeAnalysis;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.Expression;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.FunctionCallExpression;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
import com.wrmsr.tokamak.core.parse.tree.QualifiedNameExpression;
import com.wrmsr.tokamak.core.parse.tree.Select;
import com.wrmsr.tokamak.core.parse.tree.SelectItem;
import com.wrmsr.tokamak.core.parse.tree.TableName;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.parse.tree.visitor.AstVisitor;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.ProjectNode;
import com.wrmsr.tokamak.core.plan.node.Projection;
import com.wrmsr.tokamak.core.plan.node.ScanNode;
import com.wrmsr.tokamak.util.NameGenerator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public class AstPlanner
{
    private Optional<Catalog> catalog;
    private Optional<String> defaultSchema;

    private final NameGenerator nameGenerator = new NameGenerator();

    public AstPlanner(Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        this.catalog = checkNotNull(catalog);
        this.defaultSchema = checkNotNull(defaultSchema);
    }

    public AstPlanner()
    {
        this(Optional.empty(), Optional.empty());
    }

    public Node plan(TreeNode treeNode)
    {
        ScopeAnalysis scopeAnalysis = ScopeAnalysis.analyze(treeNode, catalog, defaultSchema);

        return treeNode.accept(new AstVisitor<Node, Void>()
        {
            @Override
            public Node visitSelect(Select treeNode, Void context)
            {
                TableName tableName = (TableName) treeNode.getRelations().get(0).getRelation();
                SchemaTable schemaTable = tableName.getQualifiedName().toSchemaTable(defaultSchema);

                Table table = catalog.get().getSchemaTable(schemaTable);
                Map<String, String> columnsByLabel = new LinkedHashMap<>();

                for (SelectItem item : treeNode.getItems()) {
                    if (item instanceof AllSelectItem) {
                        for (String column : table.getRowLayout().getFieldNames()) {
                            checkState(!columnsByLabel.containsKey(column));
                            columnsByLabel.put(column, column);
                        }
                    }
                    else if (item instanceof ExpressionSelectItem) {
                        ExpressionSelectItem exprItem = (ExpressionSelectItem) item;
                        Expression expr = exprItem.getExpression();
                        String column;
                        if (expr instanceof QualifiedNameExpression) {
                            QualifiedName qname = ((QualifiedNameExpression) expr).getQualifiedName();
                            List<String> qnameParts = qname.getParts();
                            if (qnameParts.size() == 1) {
                                column = qnameParts.get(0);
                            }
                            else if (qnameParts.size() == 2) {
                                checkState(qnameParts.get(0).equals(table.getName()));
                                column = qnameParts.get(1);
                            }
                            else {
                                throw new IllegalArgumentException(qnameParts.toString());
                            }
                        }
                        else if (expr instanceof FunctionCallExpression) {
                            FunctionCallExpression fexpr = (FunctionCallExpression) expr;
                            throw new IllegalArgumentException(expr.toString());
                        }
                        else {
                            throw new IllegalArgumentException(expr.toString());
                        }

                        checkState(table.getRowLayout().getFields().containsKey(column));
                        String label;
                        if (exprItem.getLabel().isPresent()) {
                            label = exprItem.getLabel().get();
                        }
                        else {
                            label = column;
                        }
                        checkState(!columnsByLabel.containsKey(label));
                        columnsByLabel.put(label, column);
                    }
                    else {
                        throw new IllegalArgumentException(item.toString());
                    }
                }

                Set<String> columns = ImmutableSet.copyOf(columnsByLabel.values());
                ScanNode scanNode = new ScanNode(
                        "scan0",
                        schemaTable,
                        columns.stream().collect(toImmutableMap(identity(), table.getRowLayout().getFields()::get)),
                        ImmutableSet.of(),
                        ImmutableSet.of());
                Node node = scanNode;

                if (!columnsByLabel.keySet().equals(columns)) {
                    node = new ProjectNode(
                            nameGenerator.get(),
                            scanNode,
                            Projection.of(columnsByLabel));
                }

                return node;
            }

            @Override
            public Node visitTableName(TableName treeNode, Void context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);

                Table table = catalog.get().getSchemaTable(schemaTable);

                Set<String> columns = new LinkedHashSet<>();

                ScopeAnalysis.Scope scope = scopeAnalysis.getScope(treeNode).get();
                scope.getSymbols().forEach(s -> {
                    checkState(table.getRowLayout().getFields().containsKey(s.getName().get()));
                    Set<ScopeAnalysis.SymbolRef> srs = scopeAnalysis.getResolutions().getSymbolRefs().get(s);
                    if (srs != null) {
                        columns.add(s.getName().get());
                    }
                });

                return new ScanNode(
                        nameGenerator.get("scan"),
                        schemaTable,
                        columns.stream().collect(toImmutableMap(identity(), table.getRowLayout().getFields()::get)),
                        ImmutableSet.of(),
                        ImmutableSet.of());
            }
        }, null);
    }
}
