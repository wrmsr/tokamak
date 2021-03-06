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
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.PlanningContext;
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
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollectors.groupingByImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
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

                public PNode getEntrypoint()
                {
                    return path.get(path.size() - 2);
                }
            }

            private final PInvalidatable invalidatble;

            private final Map<ImmutableList<PNode>, PathBuilder> pathBuilders = new LinkedHashMap<>();

            public NodeBuilder(PInvalidatable invalidatable)
            {
                this.invalidatble = checkNotNull(invalidatable);
            }

            public PathBuilder getPath(ImmutableList<PNode> path)
            {
                return pathBuilders.computeIfAbsent(path, PathBuilder::new);
            }

            public PInvalidator getInvalidator()
            {
                return invalidator;
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

    private static Set<String> buildUpdateMask(PInvalidator invalidator, PNode entrypoint, OriginAnalysis originAnalysis)
    {
        return originAnalysis.getOriginationSetsBySinkNodeBySinkField().get(entrypoint).values().stream()
                .flatMap(Set::stream)
                .filter(o -> o.getSource().isPresent())
                .map(o -> o.getSource().get())
                .filter(nf -> nf.getNode() == invalidator)
                .map(PNodeField::getField)
                .collect(toImmutableSet());
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
        Map<PInvalidator, Set<Origination>> invalidatorOriginationsByInvalidatorNode;
        try {
            invalidatorOriginationsByInvalidatorNode = searchNodeFields.stream()
                    .map(originAnalysis.getStateChainAnalysis().getFirstOriginationSetsBySink()::get)
                    .flatMap(Set::stream)
                    .collect(groupingByImmutableSet(o -> (PInvalidator) o.getSink().getNode()));
        }
        catch (Exception e) {
            throw e;
        }

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

    private static PInvalidations.NodeEntry buildNodeEntry(InvalidationsBuilder.NodeBuilder builder, OriginAnalysis originAnalysis)
    {
        Map<ImmutableMap<String, String>, List<InvalidationsBuilder.NodeBuilder.PathBuilder>> pathBuilderListsByKeyMap = new HashMap<>();
        for (InvalidationsBuilder.NodeBuilder.PathBuilder pathBuilder : builder.pathBuilders.values()) {
            pathBuilderListsByKeyMap.computeIfAbsent(pathBuilder.getKeyFieldsBySourceField(), m -> new ArrayList<>()).add(pathBuilder);
        }

        List<PInvalidations.Invalidation> invalidations = new ArrayList<>();
        pathBuilderListsByKeyMap.forEach((keyMap, pathBuilders) -> {
            Set<String> updateMask = pathBuilders.stream()
                    .flatMap(pb -> buildUpdateMask(builder.getInvalidator(), pb.getEntrypoint(), originAnalysis).stream())
                    .filter(negate(keyMap::containsKey))
                    .collect(toImmutableSet());

            invalidations.add(new PInvalidations.Invalidation(
                    keyMap,
                    Optional.of(updateMask),
                    PInvalidations.Strength.STRONG));
        });

        Set<String> updateMask = builder.pathBuilders.values().stream()
                .map(e -> SetInvalidationsTransform.buildUpdateMask(builder.getInvalidator(), e.getEntrypoint(), originAnalysis))
                .flatMap(Set::stream)
                .collect(toImmutableSet());

        return new PInvalidations.NodeEntry(
                invalidations,
                Optional.of(updateMask));
    }

    public static Plan setInvalidations(Plan plan, PlanningContext planningContext)
    {
        OriginAnalysis originAnalysis = OriginAnalysis.analyze(plan);
        IdAnalysis idAnalysis = IdAnalysis.analyze(plan, planningContext.getCatalog());

        Map<PInvalidator, InvalidationsBuilder> builders = new HashMap<>();

        plan.getNodeTypeList(PInvalidatable.class).forEach(invalidatable -> processNode(
                builders,
                invalidatable,
                originAnalysis,
                idAnalysis));

        Map<PInvalidator, PInvalidations> invalidations = immutableMapValues(builders, ib ->
                new PInvalidations(ib.nodeBuilders.values().stream().collect(toImmutableMap(
                        nb -> nb.invalidatble.getName(), nb -> buildNodeEntry(nb, originAnalysis)))));

        return Plan.of(plan.getRoot().accept(new PNodeRewriter<Void>()
        {
            @Override
            public PNode visitScan(PScan node, Void context)
            {
                return new PScan(
                        node.getName(),
                        node.getAnnotations(),
                        node.getFieldAnnotations(),
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
                        node.getFieldAnnotations(),
                        node.getSource(),
                        node.getDenormalization(),
                        invalidations.getOrDefault(node, PInvalidations.empty()));
            }
        }, null));
    }
}
