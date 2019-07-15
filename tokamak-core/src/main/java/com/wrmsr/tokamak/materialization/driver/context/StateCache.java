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
package com.wrmsr.tokamak.materialization.driver.context;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.materialization.api.Attributes;
import com.wrmsr.tokamak.materialization.api.Id;
import com.wrmsr.tokamak.materialization.driver.state.State;
import com.wrmsr.tokamak.materialization.driver.state.StateStorage;
import com.wrmsr.tokamak.materialization.driver.state.StateStorageContext;
import com.wrmsr.tokamak.materialization.node.Node;
import com.wrmsr.tokamak.materialization.node.StatefulNode;
import com.wrmsr.tokamak.materialization.plan.Plan;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class StateCache
{
    @FunctionalInterface
    public interface AttributesSetCallback
    {
        void onAttributesSet(State state);
    }

    private final Plan plan;
    private final StateStorage storage;
    private final List<AttributesSetCallback> attributesSetCallbacks;
    private final Stat.Updater statUpdater;

    private final List<Node> prioritizedNodes;
    private final Map<Node, Integer> prioritiesByNode;

    private final Set<State> states;
    private final Map<Node, Map<Id, State>> statesByIdByNode;
    private final Map<State, State.Mode> statesByMode;
    private final Map<State.Mode, Map<Integer, Set<Id>>> idSetsByNodePriorityByMode;
    private final Map<Integer, Set<Id>> pendingInvalidIdSetsByNodePriority;
    private final Set<State> attributesSetCallbackFiredStates;

    public StateCache(
            Plan plan,
            StateStorage storage,
            List<AttributesSetCallback> attributesSetCallbacks,
            Stat.Updater statUpdater)
    {
        this.plan = plan;
        this.storage = storage;
        this.attributesSetCallbacks = ImmutableList.copyOf(attributesSetCallbacks);
        this.statUpdater = statUpdater;

        prioritizedNodes = plan.getToposortedNodes().stream().filter(StatefulNode.class::isInstance).collect(toImmutableList());
        prioritiesByNode = IntStream.range(0, prioritizedNodes.size())
                .mapToObj(i -> new ImmutablePair<>(prioritizedNodes.get(i), i))
                .collect(toImmutableMap(ImmutablePair::getKey, ImmutablePair::getValue));

        states = new HashSet<>();
        statesByIdByNode = new HashMap<>();
        statesByMode = new HashMap<>();
        idSetsByNodePriorityByMode = new HashMap<>();
        pendingInvalidIdSetsByNodePriority = new HashMap<>();
        attributesSetCallbackFiredStates = new HashSet<>();
    }

    public boolean contains(State state)
    {
        return states.contains(state);
    }

    public void invalidate(StatefulNode node, Set<Id> ids)
    {
        throw new IllegalStateException();
    }

    public boolean isInvalidated(StatefulNode node, Id id)
    {
        throw new IllegalStateException();
    }

    public Optional<State> get(
            StateStorageContext storageCtx,
            StatefulNode node,
            Id id,
            boolean create,
            boolean invalidate,
            Optional<Predicate<State>> invalidateIf,
            boolean share,
            boolean noLoad)
    {
        throw new IllegalStateException();
    }

    private void trackNewState(State state)
    {
        throw new IllegalStateException();
    }

    private void trackStateStatus(State state)
    {
        throw new IllegalStateException();
    }

    private void onStateModeChange(State state, State.Mode newMode, State.Mode oldMode)
    {
        throw new IllegalStateException();
    }

    public State setPhantomAttributes(StatefulNode node, Id id, Optional<Attributes> attributes)
    {
        throw new IllegalStateException();
    }

    public void upgradePhantom(
            StateStorageContext storageCtx,
            State state,
            boolean share)
    {
        throw new IllegalStateException();
    }

    public Collection<State> getStates()
    {
        return states;
    }

    public Map<Node, Set<Id>> getInvalid()
    {
        throw new IllegalStateException();
    }

    public Optional<Pair<Node, Id>> getNextInvalid()
    {
        throw new IllegalStateException();
    }

    public Map<Id, State> getIdMap(StatefulNode node)
    {
        throw new IllegalStateException();
    }

    public void flush(StateStorageContext storageCtx)
    {
        throw new IllegalStateException();
    }
}
