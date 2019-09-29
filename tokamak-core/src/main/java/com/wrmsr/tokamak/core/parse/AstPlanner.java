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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Function;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.parse.analysis.ScopeAnalysis;
import com.wrmsr.tokamak.core.parse.tree.AliasedRelation;
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
import com.wrmsr.tokamak.core.plan.node.CrossJoinNode;
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
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;
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

        return treeNode.accept(new AstVisitor<Node, ScopeAnalysis.Scope>()
        {
            @Override
            public Node visitAliasedRelation(AliasedRelation treeNode, ScopeAnalysis.Scope context)
            {
                Node scanNode = treeNode.getRelation().accept(this, scopeAnalysis.getScope(treeNode).get());
                return new ProjectNode(
                        nameGenerator.get("aliasedRelationProject"),
                        scanNode,
                        Projection.of(
                                scanNode.getFields().keySet().stream()
                                        .collect(toImmutableMap(f -> treeNode.getAlias().get() + "." + f, identity()))));
            }

            @Override
            public Node visitSelect(Select treeNode, ScopeAnalysis.Scope context)
            {
                Map<String, Projection.Input> projection = new LinkedHashMap<>();

                for (SelectItem item : treeNode.getItems()) {
                    ExpressionSelectItem exprItem = (ExpressionSelectItem) item;
                    String label = exprItem.getLabel().get();
                    Expression expr = exprItem.getExpression();

                    if (expr instanceof QualifiedNameExpression) {
                        QualifiedName qname = ((QualifiedNameExpression) expr).getQualifiedName();
                        projection.put(label, Projection.Input.of(Joiner.on(".").join(qname.getParts())));
                    }

                    else if (expr instanceof FunctionCallExpression) {
                        FunctionCallExpression fcExpr = (FunctionCallExpression) expr;
                        Function func = catalog.get().getFunction(fcExpr.getName());
                        List<String> args = fcExpr.getArgs().stream()
                                .map(QualifiedNameExpression.class::cast)
                                .map(QualifiedNameExpression::getQualifiedName)
                                .map(QualifiedName::getParts)
                                .map(Joiner.on(".")::join)
                                .collect(toImmutableList());
                        projection.put(label, Projection.Input.of(func.asNodeFunction(), args));
                    }

                    else {
                        throw new IllegalArgumentException(expr.toString());
                    }
                }

                List<Node> sources = treeNode.getRelations().stream()
                        .map(r -> r.accept(this, null))
                        .collect(toImmutableList());
                Node source;
                if (sources.size() == 1) {
                    source = checkSingle(sources);
                }
                else {
                    source = new CrossJoinNode(
                            nameGenerator.get("projectCrossJoin"),
                            sources,
                            CrossJoinNode.Mode.INNER);
                }

                return new ProjectNode(
                        nameGenerator.get("selectProject"),
                        source,
                        new Projection(projection));
            }

            @Override
            public Node visitTableName(TableName treeNode, ScopeAnalysis.Scope context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);

                Table table = catalog.get().getSchemaTable(schemaTable);

                Set<String> columns = new LinkedHashSet<>();

                context.getSymbols().forEach(s -> {
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
