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
package com.wrmsr.tokamak.driver.context;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Payload;
import com.wrmsr.tokamak.node.GeneratorNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newIdentityHashSet;
import static com.wrmsr.tokamak.util.MoreCollections.newIdentityHashSetOf;
import static java.util.Collections.unmodifiableSet;

public class LineageTracker
{
    private final boolean noState;

    private final Map<Payload, Set<Payload>> lineagesByPayload;

    private final Map<Payload, Set<Payload>> statefulOutputLineageSetsByPayload;
    private final Map<Payload, Set<Payload>> statefulIntermediateLineageSetsByPayload;
    private final Map<Payload, Set<Payload>> statefulInputLineageSetsByPayload;

    private static final Set<Payload> EMPTY_LINEAGE = unmodifiableSet(newIdentityHashSetOf(ImmutableList.of()));

    public LineageTracker(boolean noState)
    {
        this.noState = noState;

        lineagesByPayload = new IdentityHashMap<>();

        if (noState) {
            statefulOutputLineageSetsByPayload = null;
            statefulIntermediateLineageSetsByPayload = null;
            statefulInputLineageSetsByPayload = null;
        }
        else {
            statefulOutputLineageSetsByPayload = new IdentityHashMap<>();
            statefulIntermediateLineageSetsByPayload = new IdentityHashMap<>();
            statefulInputLineageSetsByPayload = new IdentityHashMap<>();
        }
    }

    public boolean contains(Payload payload)
    {
        return lineagesByPayload.containsKey(payload);
    }

    private void trackInternal(
            Node node,
            Payload payload,
            Set<Payload> lineage)
    {
        checkState(!lineagesByPayload.containsKey(payload));

        if (node instanceof GeneratorNode) {
            checkArgument(lineage.isEmpty());
            lineage = EMPTY_LINEAGE;
        } else {
            lineage.forEach(lp -> checkArgument(lineagesByPayload.containsKey(lp)));
        }

        lineagesByPayload.put(payload, lineage);

        if (!noState) {
            Set<Payload> statefulLineage = newIdentityHashSet();
            for (Payload lp : lineage) {
                statefulLineage.addAll(statefulIntermediateLineageSetsByPayload.get(lp));
            }

            if (node instanceof StatefulNode) {
                // FIXME
            }
            else {
                statefulIntermediateLineageSetsByPayload.put(payload, statefulLineage);
            }
        }
    }

    public void track(
            Node node,
            Payload payload,
            Iterable<Payload> lineage)
    {
        Set<Payload> lineageSet = unmodifiableSet(newIdentityHashSetOf(lineage));
        checkArgument(!lineageSet.isEmpty());
        trackInternal(node, payload, lineageSet);
    }

    public void trackBlackHole(
            Node node,
            Payload payload,
            Iterable<Payload> lineage)
    {
        trackInternal(node, payload, EMPTY_LINEAGE);
    }
}
