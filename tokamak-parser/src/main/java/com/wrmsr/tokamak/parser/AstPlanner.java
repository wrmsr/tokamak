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
package com.wrmsr.tokamak.parser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.Projection;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.parser.tree.AllSelectItem;
import com.wrmsr.tokamak.parser.tree.Expression;
import com.wrmsr.tokamak.parser.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.parser.tree.Identifier;
import com.wrmsr.tokamak.parser.tree.QualifiedName;
import com.wrmsr.tokamak.parser.tree.Select;
import com.wrmsr.tokamak.parser.tree.SelectItem;
import com.wrmsr.tokamak.parser.tree.TableName;
import com.wrmsr.tokamak.parser.tree.TreeNode;
import com.wrmsr.tokamak.parser.tree.visitor.AstVisitor;
import com.wrmsr.tokamak.util.NameGenerator;

import java.util.LinkedHashMap;
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
        return treeNode.accept(new AstVisitor<Node, Void>()
        {
            @Override
            public Node visitSelect(Select treeNode, Void context)
            {
                TableName tableName = (TableName) treeNode.getRelation().get();
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

                Table table = catalog.get().lookupSchemaTable(schemaTable);
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
                        if (expr instanceof Identifier) {
                            Identifier ident = (Identifier) expr;
                            column = ident.getValue();
                        }
                        else if (expr instanceof QualifiedName) {
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
                        ImmutableSet.of(),
                        ImmutableMap.of(),
                        ImmutableMap.of(),
                        Optional.empty());
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
