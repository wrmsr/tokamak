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
package com.wrmsr.tokamak.core.driver.context;

import com.wrmsr.tokamak.core.driver.context.lineage.LineageEntry;
import com.wrmsr.tokamak.core.driver.context.state.StateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.StatefulNode;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class LinkageManager
{
    private final StateCache stateCache;

    private static final class Entry
    {
        private final State state;
        private final Set<State> inputStates = new HashSet<>();
        private final Set<State> outputStates = new HashSet<>();

        public Entry(State state)
        {
            this.state = checkNotNull(state);
        }
    }

    private final Map<State, Entry> entriesByState = new HashMap<>();

    public LinkageManager(StateCache stateCache)
    {
        this.stateCache = checkNotNull(stateCache);
    }

    public void addStateLineage(State state, Set<LineageEntry> lineage)
    {
        Entry entry = entriesByState.computeIfAbsent(state, Entry::new);
        for (LineageEntry le : lineage) {
            if (!(le.getNode() instanceof StatefulNode)) {
                continue;
            }

            StatefulNode inputNode = (StatefulNode) le.getNode();
            State inputState = stateCache.get(inputNode, le.getId(), EnumSet.of(StateCache.GetFlag.NOLOAD)).get();
            Entry inputEntry = entriesByState.computeIfAbsent(inputState, Entry::new);

            entry.inputStates.add(inputState);
            inputEntry.outputStates.add(state);
        }
    }

    private void buildStateLinkage(State state)
    {
        // state.getLinkage().getInput()
    }

    public void update()
    {
        for (State state : stateCache.getAll()) {
            buildStateLinkage(state);
        }

        throw new IllegalStateException();
    }
}
