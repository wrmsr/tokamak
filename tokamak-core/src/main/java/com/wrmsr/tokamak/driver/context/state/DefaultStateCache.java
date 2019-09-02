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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.driver.CodecManager;
import com.wrmsr.tokamak.driver.context.diag.Stat;
import com.wrmsr.tokamak.driver.state.State;
import com.wrmsr.tokamak.driver.state.StateStorage;
import com.wrmsr.tokamak.driver.state.StorageState;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.util.Pair;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public class DefaultStateCache
        implements StateCache
{
    @FunctionalInterface
    public interface AttributesSetCallback
    {
        void onAttributesSet(State state);
    }

    private final Plan plan;
    private final StateStorage storage;
    private final CodecManager codecManager;
    private final List<AttributesSetCallback> attributesSetCallbacks;
    private final Stat.Updater statUpdater;

    private final List<Node> prioritizedNodes;
    private final Map<Node, Integer> prioritiesByNode;

    private final Set<State> allStates;
    private final Map<Node, Map<Id, State>> statesByIdByNode;
    private final Map<State, State.Mode> modesByState;
    private final Map<State.Mode, SortedMap<Integer, SortedSet<Id>>> idSetsByNodePriorityByMode;
    private final SortedMap<Integer, SortedSet<Id>> pendingInvalidIdSetsByNodePriority;
    private final Set<State> attributesSetCallbackFiredStates;

    public DefaultStateCache(
            Plan plan,
            StateStorage storage,
            CodecManager codecManager,
            List<AttributesSetCallback> attributesSetCallbacks,
            Stat.Updater statUpdater)
    {
        this.plan = checkNotNull(plan);
        this.storage = checkNotNull(storage);
        this.codecManager = checkNotNull(codecManager);
        this.attributesSetCallbacks = ImmutableList.copyOf(attributesSetCallbacks);
        this.statUpdater = checkNotNull(statUpdater);

        prioritizedNodes = plan.getToposortedNodes().stream().filter(StatefulNode.class::isInstance).collect(toImmutableList());
        prioritiesByNode = IntStream.range(0, prioritizedNodes.size())
                .mapToObj(i -> new Pair.Immutable<>(prioritizedNodes.get(i), i))
                .collect(toImmutableMap(Pair.Immutable::getKey, Pair.Immutable::getValue));

        allStates = new HashSet<>();
        statesByIdByNode = new HashMap<>();
        modesByState = new HashMap<>();
        idSetsByNodePriorityByMode = new HashMap<>();
        pendingInvalidIdSetsByNodePriority = new TreeMap<>();
        attributesSetCallbackFiredStates = new HashSet<>();
    }

    @Override
    public Optional<State> get(StatefulNode node, Id id, EnumSet<GetFlag> flags)
    {
        Map<Id, State> statesById = statesByIdByNode.computeIfAbsent(node, n -> new HashMap<>());
        State state = statesById.get(id);
        if (state != null) {
            statUpdater.update(node, Stat.STATE_CACHE_HIT);
            if (flags.contains(GetFlag.INVALIDATE)) {
                checkState(state.getMode() == State.Mode.INVALID);
            }
            return Optional.of(state);
        }

        statUpdater.update(node, Stat.STATE_CACHE_MISS);

        if (flags.contains(GetFlag.NOLOAD)) {
            return Optional.empty();
        }

        long startTime = System.currentTimeMillis();
        try (StateStorage.Context storageContext = storage.createContext()) {
            EnumSet<StateStorage.GetFlag> storageFlags = EnumSet.of(StateStorage.GetFlag.LINKAGE, StateStorage.GetFlag.ATTRIBUTES);
            if (flags.contains(GetFlag.CREATE)) {
                storageFlags.add(StateStorage.GetFlag.CREATE);
            }
            Map<StatefulNode, Map<Id, StorageState>> storageResult;
            try {
                storageResult = storage.get(
                        storageContext,
                        ImmutableMap.of(node, ImmutableSet.of(id)),
                        storageFlags);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            long endTime = System.currentTimeMillis();
            statUpdater.update(node, Stat.STATE_CACHE_LOAD_TIME, (endTime - startTime));
            if (storageResult.isEmpty()) {
                checkState(!flags.contains(GetFlag.CREATE));
                return Optional.empty();
            }
            StorageState storageState = checkSingle(checkSingle(storageResult.values()).values());
            state = codecManager.getStateCodecsByNode().get(node).decode(storageState);
        }

        checkNotNull(state);
        checkState(state.getMode().isStorageMode());

        State.Mode initialMode;
        int nodePriority = prioritiesByNode.get(node);
        Set<Id> pendingInvalidationIds = pendingInvalidIdSetsByNodePriority.get(nodePriority);
        boolean isPendingInvalidation = pendingInvalidationIds != null && pendingInvalidationIds.contains(id);
        if (isPendingInvalidation || flags.contains(GetFlag.INVALIDATE)) {
            initialMode = State.Mode.INVALID;
        }
        else {
            initialMode = State.Mode.EXCLUSIVE;
        }
        state.setInitialMode(initialMode);

        trackNewState(state);
        return Optional.of(state);
    }

    @Override
    public boolean contains(State state)
    {
        return allStates.contains(state);
    }

    private void trackNewState(State state)
    {
        checkState(!allStates.contains(state));
        Map<Id, State> statesById = statesByIdByNode.computeIfAbsent(state.getNode(), n -> new HashMap<>());
        checkState(!statesById.containsKey(state.getId()));
        allStates.add(state);
        statesById.put(state.getId(), state);

        trackStateStatus(state);

        state.setModeCallback(this::onStateModeChange);
    }

    private void trackStateStatus(State state)
    {
        checkArgument(!state.getMode().isStorageMode());
        int nodePriority = prioritiesByNode.get(state.getNode());

        State.Mode oldState = modesByState.get(state);
        if (oldState != null) {
            Map<Integer, SortedSet<Id>> map = checkNotNull(idSetsByNodePriorityByMode.get(oldState));
            Set<Id> set = checkNotNull(map.get(nodePriority));
            checkState(set.contains(state.getId()));
            set.remove(state.getId());
            if (set.isEmpty()) {
                map.remove(nodePriority);
            }
            if (map.isEmpty()) {
                idSetsByNodePriorityByMode.remove(oldState);
            }
        }

        State.Mode newState = state.getMode();
        modesByState.put(state, newState);

        Map<Integer, SortedSet<Id>> map = idSetsByNodePriorityByMode.computeIfAbsent(oldState, s -> new TreeMap<>());
        Set<Id> set = map.computeIfAbsent(nodePriority, np -> new TreeSet<>());
        set.add(state.getId());

        SortedSet<Id> pendingInvalidIds = pendingInvalidIdSetsByNodePriority.get(nodePriority);
        if (pendingInvalidIds != null) {
            if (pendingInvalidIds.contains(state.getId())) {
                if (oldState != null) {
                    checkState(oldState == State.Mode.INVALID);
                }
                pendingInvalidIds.remove(state.getId());
            }
            if (pendingInvalidIds.isEmpty()) {
                pendingInvalidIdSetsByNodePriority.remove(nodePriority);
            }
        }

        if (newState != State.Mode.INVALID && !attributesSetCallbackFiredStates.contains(state)) {
            state.getAttributes();
            attributesSetCallbackFiredStates.add(state);
            for (AttributesSetCallback cb : attributesSetCallbacks) {
                cb.onAttributesSet(state);
            }
        }
    }

    private void onStateModeChange(State state, State.Mode newMode, State.Mode oldMode)
    {
        // for (AttributesSetCallback cb : attributesSetCallbacks) {
        //     cb.onAttributesSet(state, newMode, oldMode);
        // }

        throw new IllegalStateException();
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
        return allStates;
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
