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
package com.wrmsr.tokamak.core.plan.transform;

import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.PlanningContext;

public final class JoinTransform
{
    private JoinTransform()
    {
    }

    /*
                Set<Set<String>> fieldEqualitiesSet = new LinkedHashSet<>();
                if (treeNode.getWhere().isPresent()) {
                    TNode where = treeNode.getWhere().get();

                    new TNodeVisitor<Void, Void>()
                    {
                        @Override
                        public Void visitExpression(TExpression node, Void context)
                        {
                            return null;
                        }

                        @Override
                        public Void visitBooleanExpression(TBooleanExpression node, Void context)
                        {
                            if (node.getOp() == TBooleanExpression.Op.AND) {
                                process(node.getLeft(), context);
                                process(node.getRight(), context);
                            }
                            return null;
                        }

                        @Override
                        public Void visitComparisonExpression(TComparisonExpression cmp, Void context)
                        {
                            if (cmp.getLeft() instanceof TQualifiedNameExpression && cmp.getRight() instanceof TQualifiedNameExpression) {
                                Set<String> set = ImmutableList.of(cmp.getLeft(), cmp.getRight()).stream()
                                        .map(TQualifiedNameExpression.class::cast)
                                        .map(TQualifiedNameExpression::getQualifiedName)
                                        .map(TQualifiedName::toDotString)
                                        .collect(toImmutableSet());
                                fieldEqualitiesSet.add(set);
                            }
                            return null;
                        }
                    }.process(where, null);
                }
                List<Set<String>> fieldEqualities = MoreCollections.unify(fieldEqualitiesSet);

                ...

                    Set<PNode> sourcesSet = ImmutableSet.copyOf(sources);
                    List<Set<String>> joinEqualities = fieldEqualities.stream()
                            .filter(s -> s.stream()
                                    .map(f -> checkNotNull(sourcesByField.get(f)))
                                    .collect(toImmutableSet())
                                    .equals(sourcesSet))
                            .collect(toImmutableList());

                    // FIXME: JOINS FOR ANY EQUALITIES - does NOT have to be full equality to be a join
                    //   - (winds up being cartesian and iteratiely filtering lol)
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
    */

    public static Plan joinTransform(Plan plan, PlanningContext planningContext)
    {
        return plan;
    }
}
