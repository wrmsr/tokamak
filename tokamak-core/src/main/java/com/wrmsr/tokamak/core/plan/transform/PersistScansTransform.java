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
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeRewriters;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.Pair;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class PersistScansTransform
{
    private PersistScansTransform()
    {
    }

    public static Plan persistScans(Plan plan)
    {
        Map<PScan, PNode> newScans = plan.getNodeTypeList(PScan.class).stream()
                .map(scan -> {
                    Set<PNode> sinks = checkNotNull(plan.getSinkSetsBySource().get(scan));
                    PNode ret;
                    if (sinks.size() < 1 || (sinks.size() == 1 && checkSingle(sinks) instanceof PState)) {
                        ret = scan;
                    }
                    else {
                        ret = new PState(
                                plan.getNodeNameGenerator().get(scan.getName() + "$persist"),
                                AnnotationCollection.of(),
                                AnnotationCollectionMap.of(),
                                scan,
                                PState.Denormalization.NONE,
                                PInvalidations.empty());
                    }
                    return Pair.immutable(scan, ret);
                })
                .collect(toImmutableMap());

        if (newScans.isEmpty()) {
            return plan;
        }

        return Plan.of(PNodeRewriters.rewrite(plan.getRoot(), ImmutableMap.copyOf(newScans)));
    }
}
