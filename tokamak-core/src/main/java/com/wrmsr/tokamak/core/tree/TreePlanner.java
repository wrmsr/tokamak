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
package com.wrmsr.tokamak.core.tree;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Function;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PValue;
import com.wrmsr.tokamak.core.tree.analysis.SymbolAnalysis;
import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TComparisonExpression;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.TTableName;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeVisitor;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.MoreCollections;
import com.wrmsr.tokamak.util.NameGenerator;
import com.wrmsr.tokamak.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;
import static java.util.function.Function.identity;

public class TreePlanner
{
    private Optional<Catalog> catalog;
    private Optional<String> defaultSchema;

    // FIXME: ensure no collisions (node *and* field names)
    private final NameGenerator nameGenerator = new NameGenerator(ImmutableSet.of(), Plan.NAME_GENERATOR_PREFIX);

    public TreePlanner(Optional<Catalog> catalog, Optional<String> defaultSchema)
    {
        this.catalog = checkNotNull(catalog);
        this.defaultSchema = checkNotNull(defaultSchema);
    }

    public TreePlanner()
    {
        this(Optional.empty(), Optional.empty());
    }

    public PNode plan(TNode treeNode)
    {
        SymbolAnalysis symbolAnalysis = SymbolAnalysis.analyze(treeNode, catalog, defaultSchema);

        return treeNode.accept(new TNodeVisitor<PNode, SymbolAnalysis.SymbolScope>()
        {
            @Override
            public PNode visitAliasedRelation(TAliasedRelation treeNode, SymbolAnalysis.SymbolScope context)
            {
                PNode scanNode = process(treeNode.getRelation(), symbolAnalysis.getSymbolScope(treeNode).get());
                return new PProject(
                        nameGenerator.get("aliasedRelationProject"),
                        AnnotationCollection.of(),
                        AnnotationCollectionMap.of(),
                        scanNode,
                        new PProjection(
                                scanNode.getFields().getNames().stream()
                                        .collect(toImmutableMap(f -> treeNode.getAlias().get() + "." + f, PValue::field))));
            }

            @Override
            public PNode visitSelect(TSelect treeNode, SymbolAnalysis.SymbolScope context)
            {
                Map<String, PValue> projection = new LinkedHashMap<>();

                for (TSelectItem item : treeNode.getItems()) {
                    TExpressionSelectItem exprItem = (TExpressionSelectItem) item;
                    String label = exprItem.getLabel().get();
                    TExpression expr = exprItem.getExpression();

                    if (expr instanceof TQualifiedNameExpression) {
                        TQualifiedName qname = ((TQualifiedNameExpression) expr).getQualifiedName();
                        projection.put(label, PValue.field(Joiner.on(".").join(qname.getParts())));
                    }

                    else if (expr instanceof TFunctionCallExpression) {
                        TFunctionCallExpression fcExpr = (TFunctionCallExpression) expr;
                        Function func = catalog.get().getFunction(fcExpr.getName());
                        List<PValue> args = fcExpr.getArgs().stream()
                                .map(TQualifiedNameExpression.class::cast)
                                .map(TQualifiedNameExpression::getQualifiedName)
                                .map(TQualifiedName::getParts)
                                .map(Joiner.on(".")::join)
                                .map(PValue::field)
                                .collect(toImmutableList());
                        projection.put(label, PValue.function(func.asNodeFunction(), args));
                    }

                    else {
                        throw new IllegalArgumentException(expr.toString());
                    }
                }

                Set<Set<String>> fieldEqualitiesSet = new LinkedHashSet<>();
                if (treeNode.getWhere().isPresent()) {
                    TNode where = treeNode.getWhere().get();
                    if (where instanceof TComparisonExpression) {
                        TComparisonExpression cmp = (TComparisonExpression) where;
                        if (cmp.getLeft() instanceof TQualifiedNameExpression && cmp.getRight() instanceof TQualifiedNameExpression) {
                            Set<String> set = ImmutableList.of(cmp.getLeft(), cmp.getRight()).stream()
                                    .map(TQualifiedNameExpression.class::cast)
                                    .map(TQualifiedNameExpression::getQualifiedName)
                                    .map(TQualifiedName::getParts)
                                    .map(Joiner.on(".")::join)
                                    .collect(toImmutableSet());
                            fieldEqualitiesSet.add(set);
                        }
                        else {
                            throw new IllegalStateException(Objects.toString(cmp));
                        }
                    }
                    else {
                        throw new IllegalStateException(Objects.toString(where));
                    }
                }
                List<Set<String>> fieldEqualities = MoreCollections.unify(fieldEqualitiesSet);

                List<PNode> sources = immutableMapItems(treeNode.getRelations(), r -> process(r, null));
                Map<String, PNode> sourcesByField = sources.stream()
                        .flatMap(s -> s.getFields().getNames().stream().map(f -> Pair.immutable(f, s)))
                        .collect(toImmutableMap());

                PNode source;
                if (sources.size() == 1) {
                    source = checkSingle(sources);
                }
                else {
                    Set<PNode> sourcesSet = ImmutableSet.copyOf(sources);
                    List<Set<String>> joinEqualities = fieldEqualities.stream()
                            .filter(s -> s.stream()
                                    .map(f -> checkNotNull(sourcesByField.get(f)))
                                    .collect(toImmutableSet())
                                    .equals(sourcesSet))
                            .collect(toImmutableList());

                    Map<PNode, List<String>> unifiedJoinEqualities = new LinkedHashMap<>();
                    Map<PNode, Map<String, Set<String>>> sourceFieldUnifications = new LinkedHashMap<>();
                    Set<String> seen = new LinkedHashSet<>();
                    for (Set<String> joinEquality : joinEqualities) {
                        Map<PNode, Set<String>> eqFieldSetsByNode = immutableMapValues(
                                joinEquality.stream()
                                        .collect(Collectors.groupingBy(sourcesByField::get)),
                                ImmutableSet::copyOf);
                        checkState(eqFieldSetsByNode.keySet().equals(sourcesSet));

                        eqFieldSetsByNode.forEach((eqNode, eqFields) -> {
                            for (String eqField : checkNotEmpty(eqFields)) {
                                checkState(!seen.contains(eqField));
                                seen.add(eqField);
                            }

                            String unifiedField;
                            if (eqFields.size() > 1) {
                                unifiedField = nameGenerator.get("unified");
                                sourceFieldUnifications.computeIfAbsent(eqNode, n -> new LinkedHashMap<>())
                                        .put(unifiedField, eqFields);
                            }
                            else {
                                unifiedField = checkSingle(eqFields);
                            }

                            unifiedJoinEqualities.computeIfAbsent(eqNode, n -> new ArrayList<>())
                                    .add(unifiedField);
                        });
                    }

                    List<PJoin.Branch> branches;
                    if (!unifiedJoinEqualities.isEmpty()) {
                        checkState(unifiedJoinEqualities.keySet().equals(sourcesSet));
                        checkSingle(unifiedJoinEqualities.values().stream().map(List::size).collect(toImmutableSet()));
                        branches = sources.stream()
                                .map(joinSource -> {
                                    Map<String, Set<String>> unifiedFields = sourceFieldUnifications.get(joinSource);
                                    PNode unifiedJoinSource;
                                    if (unifiedFields != null) {
                                        // FIXME: FILTER EQUAL
                                        // FIXME: lol, uh, outer/full... null for unified field? hmmph..
                                        // PNode unifiedJoinSource = new PUnify(
                                        //
                                        // )
                                        throw new IllegalStateException();
                                    }
                                    else {
                                        unifiedJoinSource = joinSource;
                                    }

                                    return new PJoin.Branch(unifiedJoinSource, checkNotNull(unifiedJoinEqualities.get(joinSource)));
                                })
                                .collect(toImmutableList());
                    }
                    else {
                        branches = sources.stream()
                                .map(joinSource -> new PJoin.Branch(joinSource, ImmutableList.of()))
                                .collect(toImmutableList());
                    }

                    source = new PJoin(
                            nameGenerator.get("projectJoin"),
                            AnnotationCollection.of(),
                            AnnotationCollectionMap.of(),
                            branches,
                            PJoin.Mode.FULL);
                }

                return new PProject(
                        nameGenerator.get("selectProject"),
                        AnnotationCollection.of(),
                        AnnotationCollectionMap.of(),
                        source,
                        new PProjection(projection));
            }

            @Override
            public PNode visitTableName(TTableName treeNode, SymbolAnalysis.SymbolScope context)
            {
                SchemaTable schemaTable = treeNode.getQualifiedName().toSchemaTable(defaultSchema);

                Table table = catalog.get().getSchemaTable(schemaTable);

                Set<String> columns = new LinkedHashSet<>();

                context.getSymbols().forEach(s -> {
                    checkState(table.getRowLayout().getFields().contains(s.getName().get()));
                    Set<SymbolAnalysis.SymbolRef> srs = symbolAnalysis.getResolutions().getSymbolRefs().get(s);
                    if (srs != null) {
                        columns.add(s.getName().get());
                    }
                });

                return new PScan(
                        nameGenerator.get("scan"),
                        AnnotationCollection.of(),
                        AnnotationCollectionMap.of(),
                        schemaTable,
                        columns.stream().collect(toImmutableMap(identity(), table.getRowLayout().getFields()::getType)),
                        PInvalidations.empty());
            }
        }, null);
    }
}
