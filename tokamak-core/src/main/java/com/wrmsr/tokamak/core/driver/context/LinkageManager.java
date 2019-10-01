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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.context.lineage.LineageEntry;
import com.wrmsr.tokamak.core.driver.context.state.StateCache;
import com.wrmsr.tokamak.core.driver.state.Linkage;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.PNodeId;
import com.wrmsr.tokamak.core.plan.node.PState;

import javax.annotation.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

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
            if (!(le.getNode() instanceof PState)) {
                continue;
            }

            PState inputNode = (PState) le.getNode();
            State inputState = stateCache.get(inputNode, le.getId(), EnumSet.of(StateCache.GetFlag.NOLOAD)).get();
            Entry inputEntry = entriesByState.computeIfAbsent(inputState, Entry::new);

            entry.inputStates.add(inputState);
            inputEntry.outputStates.add(state);
        }
    }

    private Map<PNodeId, Linkage.Links> buildLinks(Set<State> states, Map<PNodeId, Linkage.Links> existingLinks)
    {
        Map<PNodeId, Set<Id>> newInputLinkage = new HashMap<>();

        existingLinks.forEach((nodeId, links) -> {
            if (links instanceof Linkage.IdLinks) {
                Linkage.IdLinks idLinks = (Linkage.IdLinks) links;
                newInputLinkage.computeIfAbsent(nodeId, sn -> new HashSet<>()).addAll(idLinks.getIds());
            }
            else {
                throw new IllegalStateException(Objects.toString(links));
            }
        });

        for (State inputState : states) {
            newInputLinkage.computeIfAbsent(inputState.getNode().getId(), sn -> new HashSet<>()).add(inputState.getId());
        }

        return newInputLinkage.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> new Linkage.IdLinks(e.getValue())));
    }

    private Linkage buildLinkage(Entry entry)
    {
        @Nullable Linkage existingLinkage = entry.state.getLinkage();
        Map<PNodeId, Linkage.Links> inputLinks = buildLinks(
                entry.inputStates, existingLinkage != null ? existingLinkage.getInput() : ImmutableMap.of());
        Map<PNodeId, Linkage.Links> outputLinks = buildLinks(
                entry.outputStates, existingLinkage != null ? existingLinkage.getOutput() : ImmutableMap.of());
        return new Linkage(inputLinks, outputLinks);
    }

    public void update()
    {
        for (Entry entry : entriesByState.values()) {
            Linkage linkage = buildLinkage(entry);
            entry.state.setLinkage(linkage);
        }
    }
}
