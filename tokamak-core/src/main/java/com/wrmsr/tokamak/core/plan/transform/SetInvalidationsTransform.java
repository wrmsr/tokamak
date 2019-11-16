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
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PInvalidator;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriter;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

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
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollectors.groupingByImmutableSet;
import static com.wrmsr.tokamak.util.MoreFunctions.negate;

public final class SetInvalidationsTransform
{
    /*
    TODO:
     - PInvalidate - not just PStates (for which persistence is mandatory due to linkage tracking)
     - weaks
    */

    private SetInvalidationsTransform()
    {
    }

    private static final class InvalidationsBuilder
    {
        private final class NodeBuilder
        {
            private final class PathBuilder
            {
                private final ImmutableList<PNode> path;

                private final ImmutableMap.Builder<String, String> keyFieldsBySourceFieldBuilder = ImmutableMap.builder();

                public PathBuilder(ImmutableList<PNode> path)
                {
                    this.path = checkNotNull(path);
                }

                private final SupplierLazyValue<ImmutableMap<String, String>> keyFieldsBySourceField = new SupplierLazyValue<>();

                public ImmutableMap<String, String> getKeyFieldsBySourceField()
                {
                    return keyFieldsBySourceField.get(keyFieldsBySourceFieldBuilder::build);
                }
            }

            private final PInvalidatable invalidatble;

            private final Map<ImmutableList<PNode>, PathBuilder> pathBuilders = new LinkedHashMap<>();

            public NodeBuilder(PInvalidatable invalidatble)
            {
                this.invalidatble = checkNotNull(invalidatble);
            }

            public PathBuilder getPath(ImmutableList<PNode> path)
            {
                return pathBuilders.computeIfAbsent(path, PathBuilder::new);
            }

            public Set<String> buildUpdateMask(Set<String> keyFields, OriginAnalysis originAnalysis)
            {
                return pathBuilders.values().stream()
                        .map(e -> {
                            PNode entrypoint = e.path.get(e.path.size() - 2);
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

        private final PInvalidator invalidator;

        private final Map<PInvalidatable, NodeBuilder> nodeBuilders = new LinkedHashMap<>();

        public InvalidationsBuilder(PInvalidator invalidator)
        {
            this.invalidator = checkNotNull(invalidator);
        }

        public NodeBuilder getNode(PInvalidatable node)
        {
            return nodeBuilders.computeIfAbsent(node, NodeBuilder::new);
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

    private static void processField(
            Map<PInvalidator, InvalidationsBuilder> builders,
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
            InvalidationsBuilder builder = builders
                    .computeIfAbsent(invalidator, InvalidationsBuilder::new);

            for (Origination invalidatorOrigination : invalidatorOriginations) {
                checkState(invalidatorOrigination.getSink().getNode() instanceof PInvalidator);
                checkState(invalidatorOrigination.getSink().getNode() != invalidatable);

                PNodeField sourceNodeField = invalidatorOrigination.getSink();
                Iterable<List<OriginationLink>> originationPaths =
                        originAnalysis.getLeafChainAnalysis().getPaths(sinkNodeField, sourceNodeField);
                for (List<OriginationLink> originationPath : originationPaths) {
                    ImmutableList<PNode> nodePath = buildNodePath(sinkNodeField, sourceNodeField, originationPath);
                    builder.getNode(invalidatable).getPath(nodePath)
                            .keyFieldsBySourceFieldBuilder.put(invalidatorOrigination.getSink().getField(), sinkNodeField.getField());
                }
            }
        });
    }

    private static void processNode(
            Map<PInvalidator, InvalidationsBuilder> builders,
            PInvalidatable invalidatable,
            OriginAnalysis originAnalysis,
            IdAnalysis idAnalysis)
    {
        for (IdAnalysisPart part : idAnalysis.get(invalidatable).getParts()) {
            for (String field : part) {
                processField(
                        builders,
                        invalidatable,
                        field,
                        originAnalysis);
            }
        }
    }

    public static Plan setInvalidations(Plan plan, Optional<Catalog> catalog)
    {
        OriginAnalysis originAnalysis = OriginAnalysis.analyze(plan);
        IdAnalysis idAnalysis = IdAnalysis.analyze(plan, catalog);

        Map<PInvalidator, InvalidationsBuilder> builders = new HashMap<>();

        plan.getNodeTypeList(PInvalidatable.class).forEach(invalidatable -> processNode(
                builders,
                invalidatable,
                originAnalysis,
                idAnalysis));

        Map<PInvalidator, PInvalidations> invalidations = immutableMapValues(builders, builder -> {
            return PInvalidations.empty();
        });

        // for (InvalidationsBuilder builder : buildersByInvalidator.values()) {
        //     Map<ImmutableMap<String, String>, Set<InvalidationsBuilder.PathBuilder>> entrySetsByKeyMaps =
        //             builder.entriesByNodePath.values().stream()
        //                     .collect(groupingByImmutableSet(e -> e.keyFieldsBySourceField.build()));
        //
        //     entrySetsByKeyMaps.forEach((keyMap, entrySet) -> {
        //         Set<String> linkageMask = builder.buildLinkageMask(originAnalysis, keyMap.keySet());
        //
        //         PInvalidation invalidation = new PInvalidation(
        //                 invalidatable.getName(),
        //                 keyMap,
        //                 Optional.of(linkageMask),
        //                 PInvalidation.Strength.STRONG);
        //
        //         invalidationsByInvalidator.computeIfAbsent(builder.invalidator, o -> new ArrayList<>()).add(invalidation);
        //     });
        // }

        return Plan.of(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitScan(PScan node, Void context)
            {
                return new PScan(
                        node.getName(),
                        node.getAnnotations(),
                        node.getSchemaTable(),
                        node.getScanFields(),
                        invalidations.getOrDefault(node, PInvalidations.empty()));
            }

            @Override
            public PNode visitState(PState node, Void context)
            {
                return new PState(
                        node.getName(),
                        node.getAnnotations(),
                        node.getSource(),
                        node.getDenormalization(),
                        invalidations.getOrDefault(node, PInvalidations.empty()));
            }
        }, null));
    }
}
