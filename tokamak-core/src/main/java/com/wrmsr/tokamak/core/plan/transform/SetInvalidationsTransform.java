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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.analysis.id.IdAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.id.part.IdAnalysisPart;
import com.wrmsr.tokamak.core.plan.analysis.origin.OriginAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.origin.Origination;
import com.wrmsr.tokamak.core.plan.analysis.origin.OriginationLink;
import com.wrmsr.tokamak.core.plan.node.PInvalidatable;
import com.wrmsr.tokamak.core.plan.node.PInvalidation;
import com.wrmsr.tokamak.core.plan.node.PInvalidator;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.groupingByImmutableSet;
import static com.wrmsr.tokamak.util.MoreFunctions.negate;

public final class SetInvalidationsTransform
{
    private SetInvalidationsTransform()
    {
    }

    private static final class InvalidationConstruction
    {
        private final PInvalidator invalidator;

        private static final class PathEntry
        {
            private final ImmutableList<PNode> nodePath;

            private final ImmutableMap.Builder<String, String> keyFieldsBySourceField = ImmutableMap.builder();

            public PathEntry(ImmutableList<PNode> nodePath)
            {
                this.nodePath = checkNotNull(nodePath);
            }
        }

        private final Map<ImmutableList<PNode>, PathEntry> entriesByNodePath = new LinkedHashMap<>();

        public InvalidationConstruction(PInvalidator invalidator)
        {
            this.invalidator = checkNotNull(invalidator);
        }

        public Set<String> buildLinkageMask(OriginAnalysis originAnalysis, Set<String> keyFields)
        {
            return entriesByNodePath.values().stream()
                    .map(e -> {
                        PNode entrypoint = e.nodePath.get(e.nodePath.size() - 2);
                        return originAnalysis.getOriginationSetsBySinkNodeBySinkField().get(entrypoint).values().stream()
                                .flatMap(Set::stream)
                                .filter(o -> o.getSource().isPresent())
                                .map(o -> o.getSource().get())
                                .filter(nf -> nf.getNode() == invalidator)
                                .map(PNodeField::getField)
                                .collect(toImmutableSet());
                    })
                    .flatMap(Set::stream)
                    .filter(negate(keyFields::contains))
                    .collect(toImmutableSet());
        }
    }

    private static ImmutableList<PNode> buildNodePath(
            PNodeField sinkNodeField,
            PNodeField sourceNodeField,
            List<OriginationLink> originationPath)
    {
        checkState(originationPath.size() >= 2);
        checkState(originationPath.get(0).getSink().getSink().equals(sinkNodeField));
        checkState(originationPath.get(originationPath.size() - 1).getSink().getSink().equals(sourceNodeField));
        PNode entrypoint = originationPath.get(originationPath.size() - 2).getSink().getSink().getNode();
        checkState(entrypoint.getSources().contains(sourceNodeField.getNode()));
        return originationPath.stream()
                .map(l -> l.getSink().getSink().getNode())
                .collect(toImmutableList());
    }

    private static void addConstructionsForField(
            Map<PInvalidator, InvalidationConstruction> constructionsByInvalidator,
            PInvalidatable invalidatable,
            String field,
            OriginAnalysis originAnalysis)
    {
        PNodeField sinkNodeField = PNodeField.of(invalidatable, field);
        Set<PNodeField> searchNodeFields = invalidatable instanceof PState ?
                originAnalysis.getOriginationSetsBySink().get(sinkNodeField).stream()
                        .map(o -> o.getSource().get())
                        .collect(toImmutableSet()) :
                ImmutableSet.of(sinkNodeField);
        Map<PInvalidator, Set<Origination>> invalidatorOriginationsByInvalidatorNode = searchNodeFields.stream()
                .map(originAnalysis.getStateChainAnalysis().getFirstOriginationSetsBySink()::get)
                .flatMap(Set::stream)
                .collect(groupingByImmutableSet(o -> (PInvalidator) o.getSink().getNode()));

        invalidatorOriginationsByInvalidatorNode.forEach((invalidator, invalidatorOriginations) -> {
            InvalidationConstruction construction = constructionsByInvalidator
                    .computeIfAbsent(invalidator, InvalidationConstruction::new);

            for (Origination invalidatorOrigination : invalidatorOriginations) {
                checkState(invalidatorOrigination.getSink().getNode() instanceof PInvalidator);
                checkState(invalidatorOrigination.getSink().getNode() != invalidatable);

                PNodeField sourceNodeField = invalidatorOrigination.getSink();
                Iterable<List<OriginationLink>> originationPaths =
                        originAnalysis.getLeafChainAnalysis().getPaths(sinkNodeField, sourceNodeField);
                for (List<OriginationLink> originationPath : originationPaths) {
                    ImmutableList<PNode> nodePath = buildNodePath(sinkNodeField, sourceNodeField, originationPath);
                    construction.entriesByNodePath.computeIfAbsent(nodePath, InvalidationConstruction.PathEntry::new)
                            .keyFieldsBySourceField.put(invalidatorOrigination.getSink().getField(), sinkNodeField.getField());
                }
            }
        });
    }

    private static void addInvalidationsForNode(
            Map<PInvalidator, List<PInvalidation>> invalidationsByInvalidator,
            PInvalidatable invalidatable,
            OriginAnalysis originAnalysis,
            IdAnalysis idAnalysis)
    {
        Map<PInvalidator, InvalidationConstruction> constructionsByInvalidator = new HashMap<>();

        for (IdAnalysisPart part : idAnalysis.get(invalidatable).getParts()) {
            for (String field : part) {
                addConstructionsForField(
                        constructionsByInvalidator,
                        invalidatable,
                        field,
                        originAnalysis);
            }
        }

        for (InvalidationConstruction construction : constructionsByInvalidator.values()) {
            Map<ImmutableMap<String, String>, Set<InvalidationConstruction.PathEntry>> entrySetsByKeyMaps =
                    construction.entriesByNodePath.values().stream()
                            .collect(groupingByImmutableSet(e -> e.keyFieldsBySourceField.build()));

            entrySetsByKeyMaps.forEach((keyMap, entrySet) -> {
                Set<String> linkageMask = construction.buildLinkageMask(originAnalysis, keyMap.keySet());

                PInvalidation invalidation = new PInvalidation(
                        invalidatable.getName(),
                        keyMap,
                        Optional.of(linkageMask),
                        PInvalidation.Strength.STRONG);

                invalidationsByInvalidator.computeIfAbsent(construction.invalidator, o -> new ArrayList<>()).add(invalidation);
            });
        }
    }

    public static Plan setInvalidations(Plan plan, Optional<Catalog> catalog)
    {
        OriginAnalysis originAnalysis = OriginAnalysis.analyze(plan);
        IdAnalysis idAnalysis = IdAnalysis.analyze(plan, catalog);

        Map<PInvalidator, List<PInvalidation>> invalidationsByInvalidator = new HashMap<>();

        plan.getNodeTypeList(PInvalidatable.class).forEach(invalidatable -> {
            addInvalidationsForNode(
                    invalidationsByInvalidator,
                    invalidatable,
                    originAnalysis,
                    idAnalysis);
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
