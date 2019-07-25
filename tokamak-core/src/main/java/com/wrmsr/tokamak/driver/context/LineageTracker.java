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
import com.wrmsr.tokamak.api.Row;
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

    private final Map<Row, Set<Row>> lineagesByRow;

    private final Map<Row, Set<Row>> statefulOutputLineageSetsByRow;
    private final Map<Row, Set<Row>> statefulIntermediateLineageSetsByRow;
    private final Map<Row, Set<Row>> statefulInputLineageSetsByRow;

    private static final Set<Row> EMPTY_LINEAGE = unmodifiableSet(newIdentityHashSetOf(ImmutableList.of()));

    public LineageTracker(boolean noState)
    {
        this.noState = noState;

        lineagesByRow = new IdentityHashMap<>();

        if (noState) {
            statefulOutputLineageSetsByRow = null;
            statefulIntermediateLineageSetsByRow = null;
            statefulInputLineageSetsByRow = null;
        }
        else {
            statefulOutputLineageSetsByRow = new IdentityHashMap<>();
            statefulIntermediateLineageSetsByRow = new IdentityHashMap<>();
            statefulInputLineageSetsByRow = new IdentityHashMap<>();
        }
    }

    public boolean contains(Row row)
    {
        return lineagesByRow.containsKey(row);
    }

    private void trackInternal(
            Node node,
            Row row,
            Set<Row> lineage)
    {
        checkState(!lineagesByRow.containsKey(row));

        if (node instanceof GeneratorNode) {
            checkArgument(lineage.isEmpty());
            lineage = EMPTY_LINEAGE;
        } else {
            lineage.forEach(lp -> checkArgument(lineagesByRow.containsKey(lp)));
        }

        lineagesByRow.put(row, lineage);

        if (!noState) {
            Set<Row> statefulLineage = newIdentityHashSet();
            for (Row lp : lineage) {
                statefulLineage.addAll(statefulIntermediateLineageSetsByRow.get(lp));
            }

            if (node instanceof StatefulNode) {
                // FIXME
            }
            else {
                statefulIntermediateLineageSetsByRow.put(row, statefulLineage);
            }
        }
    }

    public void track(
            Node node,
            Row row,
            Iterable<Row> lineage)
    {
        Set<Row> lineageSet = unmodifiableSet(newIdentityHashSetOf(lineage));
        checkArgument(!lineageSet.isEmpty());
        trackInternal(node, row, lineageSet);
    }

    public void trackBlackHole(
            Node node,
            Row row,
            Iterable<Row> lineage)
    {
        trackInternal(node, row, EMPTY_LINEAGE);
    }
}
