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
package com.wrmsr.tokamak.driver.context.state;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.driver.context.diag.Stat;
import com.wrmsr.tokamak.driver.state.State;
import com.wrmsr.tokamak.driver.state.StateStorage;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.util.Pair;

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

public class StateCacheImpl
        implements StateCache
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

    public StateCacheImpl(
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
                .mapToObj(i -> new Pair.Immutable<>(prioritizedNodes.get(i), i))
                .collect(toImmutableMap(Pair.Immutable::getKey, Pair.Immutable::getValue));

        states = new HashSet<>();
        statesByIdByNode = new HashMap<>();
        statesByMode = new HashMap<>();
        idSetsByNodePriorityByMode = new HashMap<>();
        pendingInvalidIdSetsByNodePriority = new HashMap<>();
        attributesSetCallbackFiredStates = new HashSet<>();
    }

    @Override
    public Optional<State> get(StatefulNode node, Id id)
    {
        return Optional.empty();
    }

    @Override
    public boolean contains(State state)
    {
        return states.contains(state);
    }

    @Override
    public void invalidate(StatefulNode node, Set<Id> ids)
    {
        throw new IllegalStateException();
    }

    @Override
    public boolean isInvalidated(StatefulNode node, Id id)
    {
        throw new IllegalStateException();
    }

    public Optional<State> getAll(
            StateStorage.Context storageCtx,
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

    @Override
    public State createPhantom(StatefulNode node, Row row)
    {
        return null;
    }

    @Override
    public State setPhantomAttributes(StatefulNode node, Row row)
    {
        throw new IllegalStateException();
    }

    @Override
    public Collection<State> getAll()
    {
        return states;
    }

    @Override
    public Map<Node, Set<Id>> getInvalid()
    {
        throw new IllegalStateException();
    }

    @Override
    public Optional<Pair.Immutable<Node, Id>> getNextInvalid()
    {
        throw new IllegalStateException();
    }

    @Override
    public Map<Id, State> getIdMap(StatefulNode node)
    {
        throw new IllegalStateException();
    }

    @Override
    public void flush(StateStorage.Context storageCtx)
    {
        throw new IllegalStateException();
    }
}
