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
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.PlanningContext;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.annotation.ExposedPNode;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriters;
import com.wrmsr.tokamak.core.type.TypeAnnotations;
import com.wrmsr.tokamak.core.type.hier.annotation.InternalType;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class DropExposedInternalFieldsTransform
{
    private DropExposedInternalFieldsTransform()
    {
    }

    public static Plan dropExposedInternalFields(Plan plan)
    {
        List<PNode> exposedNodes = plan.getNodeListsByAnnotationType(ExposedPNode.class);
        if (exposedNodes.isEmpty()) {
            return plan;
        }

        Map<PNode, PNode> newNodes = exposedNodes.stream()
                .map(node -> {
                    Set<String> internalFields = node.getFields().stream()
                            .filter(f -> TypeAnnotations.has(f.getType(), InternalType.class))
                            .map(Field::getName)
                            .collect(toImmutableSet());
                    if (internalFields.isEmpty()) {
                        return Pair.immutable(node, node);
                    }

                    ExposedPNode ann = node.getAnnotations().get(ExposedPNode.class).get();
                    PNode newNode = PNodeRewriters.rewriteOne(
                            node,
                            Optional.empty(),
                            Optional.of(node.getAnnotations().dropped(ExposedPNode.class)),
                            Optional.empty());

                    PNode drop = new PProject(
                            plan.getNodeNameGenerator().get(node.getName() + "$dropExposedInternal"),
                            AnnotationCollection.of(ann),
                            AnnotationCollectionMap.of(),
                            newNode,
                            PProjection.only(Sets.difference(node.getFields().getNames(), internalFields)));

                    return Pair.immutable(node, drop);
                })
                .collect(toImmutableMap());

        return Plan.of(PNodeRewriters.rewrite(plan.getRoot(), ImmutableMap.copyOf(newNodes)));
    }
}
