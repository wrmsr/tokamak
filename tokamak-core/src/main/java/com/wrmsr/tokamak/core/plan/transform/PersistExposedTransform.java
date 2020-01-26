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
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.annotation.ExposedPNode;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriters;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class PersistExposedTransform
{
    private PersistExposedTransform()
    {
    }

    public static Plan persistExposed(Plan plan)
    {
        List<PNode> exposedNodes = plan.getNodeListsByAnnotationType(ExposedPNode.class);
        if (exposedNodes.isEmpty()) {
            return plan;
        }

        Map<PNode, PNode> newNodes = exposedNodes.stream()
                .map(node -> {
                    if (node instanceof PState) {
                        return Pair.immutable(node, node);
                    }

                    ExposedPNode ann = node.getAnnotations().get(ExposedPNode.class).get();

                    PNode newNode = PNodeRewriters.rewriteOne(
                            node,
                            Optional.empty(),
                            Optional.of(node.getAnnotations().dropped(ExposedPNode.class)),
                            Optional.empty());

                    PState state = new PState(
                            plan.getNodeNameGenerator().get(node.getName() + "$persist"),
                            AnnotationCollection.of(ann),
                            AnnotationCollectionMap.of(),
                            newNode,
                            PState.Denormalization.NONE,
                            PInvalidations.empty());

                    return Pair.<PNode, PNode>immutable(node, state);
                })
                .collect(toImmutableMap());

        return Plan.of(PNodeRewriters.rewrite(plan.getRoot(), ImmutableMap.copyOf(newNodes)));
    }
}
