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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.tree.AllSelectItem;
import com.wrmsr.tokamak.core.parse.tree.Expression;
import com.wrmsr.tokamak.core.parse.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.core.parse.tree.FunctionCallExpression;
import com.wrmsr.tokamak.core.parse.tree.QualifiedName;
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

import java.util.HashSet;
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

    private final static class ProjectionExpression
    {
        private final List<QualifiedName> qualifiedNames;

        private final Map<String, Set<String>> columnSetsByTable;
        private final Set<String> rawColumns;

        public ProjectionExpression(List<QualifiedName> qualifiedNames)
        {
            this.qualifiedNames = ImmutableList.copyOf(qualifiedNames);

            Map<String, Set<String>> tableNamesBySchema = new LinkedHashMap<>();
            Set<String> rawTableNames = new LinkedHashSet<>();
            this.qualifiedNames.forEach(qn -> {
                if (qn.getParts().size() == 1) {
                    rawTableNames.add(qn.getParts().get(0));
                }
                else if (qn.getParts().size() == 2) {
                    tableNamesBySchema.computeIfAbsent(qn.getParts().get(0), s -> new HashSet<>()).add(qn.getParts().get(1));
                }
            });
            this.columnSetsByTable = tableNamesBySchema.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> ImmutableSet.copyOf(e.getValue())));
            this.rawColumns = ImmutableSet.copyOf(rawTableNames);
        }
    }

    protected void addRecursiveExpressionReferences(Set<List<String>> set, Expression expr)
    {
        expr.accept(new AstVisitor<Void, Void>()
        {
            @Override
            public Void visitExpression(Expression treeNode, Void context)
            {
                return null;
            }

            @Override
            public Void visitFunctionCallExpression(FunctionCallExpression treeNode, Void context)
            {
                treeNode.getArgs().forEach(a -> addRecursiveExpressionReferences(set, a));
                return null;
            }

            @Override
            public Void visitQualifiedName(QualifiedName treeNode, Void context)
            {
                set.add(treeNode.getParts());
                return null;
            }
        }, null);
    }

    protected Set<List<String>> getRecursiveExpressionReferences(Expression expr)
    {
        Set<List<String>> qualifiedNames = new LinkedHashSet<>();
        addRecursiveExpressionReferences(qualifiedNames, expr);
        return qualifiedNames;
    }

    public Node plan(TreeNode treeNode)
    {
        return treeNode.accept(new AstVisitor<Node, Void>()
        {
            @Override
            public Node visitSelect(Select treeNode, Void context)
            {
                TableName tableName = (TableName) treeNode.getRelations().get(0).getRelation();
                List<String> tableNameParts = tableName.getQualifiedName().getParts();
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
                        Set<List<String>> qns = getRecursiveExpressionReferences(expr);
                        String column;
                        if (expr instanceof QualifiedName) {
                            QualifiedName qname = (QualifiedName) expr;
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
        }, null);
    }
}
