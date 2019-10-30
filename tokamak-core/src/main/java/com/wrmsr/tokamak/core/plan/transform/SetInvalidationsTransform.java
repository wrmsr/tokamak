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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.analysis.id.IdAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.id.part.IdAnalysisPart;
import com.wrmsr.tokamak.core.plan.analysis.origin.OriginAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.origin.Origination;
import com.wrmsr.tokamak.core.plan.node.PInvalidatable;
import com.wrmsr.tokamak.core.plan.node.PInvalidation;
import com.wrmsr.tokamak.core.plan.node.PInvalidator;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.groupingByImmutableSet;

public final class SetInvalidationsTransform
{
    private SetInvalidationsTransform()
    {
    }

    private static final class InvalidationConstruction
    {
        private final PInvalidator invalidator;

        private final Set<String> linkedInvalidatorFields = new LinkedHashSet<>();
        private final ImmutableMap.Builder<String, String> keyFieldsBySourceField = ImmutableMap.builder();

        public InvalidationConstruction(PInvalidator invalidator)
        {
            this.invalidator = invalidator;
        }
    }

    public static Plan setInvalidations(Plan plan, Optional<Catalog> catalog)
    {
        OriginAnalysis originAnalysis = OriginAnalysis.analyze(plan);
        IdAnalysis idAnalysis = IdAnalysis.analyze(plan, catalog);

        Map<PInvalidator, List<PInvalidation>> invalidationMap = new HashMap<>();

        plan.getNodeTypeList(PInvalidatable.class).forEach(invalidatable -> {
            Map<PInvalidator, InvalidationConstruction> constructions = new HashMap<>();

            for (IdAnalysisPart part : idAnalysis.get(invalidatable).getParts()) {
                for (String field : part) {
                    PNodeField nodeField = PNodeField.of(invalidatable, field);
                    Set<PNodeField> searchNodeFields = invalidatable instanceof PState ?
                            originAnalysis.getOriginationSetsBySink().get(nodeField).stream()
                                    .map(o -> o.getSource().get())
                                    .collect(toImmutableSet()) :
                            ImmutableSet.of(nodeField);
                    Map<PInvalidator, Set<Origination>> invalidatorOriginationsByInvalidatorNode = searchNodeFields.stream()
                            .map(originAnalysis.getStateChainAnalysis().getFirstOriginationSetsBySink()::get)
                            .flatMap(Set::stream)
                            .collect(groupingByImmutableSet(o -> (PInvalidator) o.getSink().getNode()));

                    invalidatorOriginationsByInvalidatorNode.forEach((invalidator, invalidatorOriginations) -> {
                        InvalidationConstruction construction = constructions.computeIfAbsent(invalidator, InvalidationConstruction::new);

                        invalidatorOriginations.forEach(invalidatorOrigination -> {
                            checkState(invalidatorOrigination.getSink().getNode() instanceof PInvalidator);
                            checkState(invalidatorOrigination.getSink().getNode() != invalidatable);

                            construction.keyFieldsBySourceField.put(invalidatorOrigination.getSink().getField(), nodeField.getField());

                            PNodeField sourceNodeField = invalidatorOrigination.getSink();
                            originAnalysis.getLeafChainAnalysis().getPaths(nodeField, sourceNodeField).forEach(path -> {
                                checkState(path.size() >= 2);
                                checkState(path.get(0).getSink().getSink().equals(nodeField));
                                checkState(path.get(path.size() - 1).getSink().getSink().equals(sourceNodeField));
                                PNode entrypoint = path.get(path.size() - 2).getSink().getSink().getNode();
                                checkState(entrypoint.getSources().contains(sourceNodeField.getNode()));
                                originAnalysis.getOriginationSetsBySinkNodeBySinkField().get(entrypoint).values()
                                        .forEach(os -> os.forEach(o -> construction.linkedInvalidatorFields.add(o.getSource().get().getField())));
                            });
                        });
                    });
                }
            }

            constructions.values().forEach(construction -> {
                PInvalidation invalidation = new PInvalidation(
                        invalidatable.getName(),
                        construction.keyFieldsBySourceField.build(),
                        Optional.of(construction.linkedInvalidatorFields),
                        PInvalidation.Strength.STRONG);

                invalidationMap.computeIfAbsent(construction.invalidator, o -> new ArrayList<>()).add(invalidation);
            });
        });

        return Plan.of(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitState(PState node, Void context)
            {
                return super.visitState(node, context);
            }
        }, null));
    }
}
