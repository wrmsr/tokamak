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
package com.wrmsr.tokamak.core.driver.context.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.core.driver.SerdeManager;
import com.wrmsr.tokamak.core.driver.context.diag.Stat;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.driver.state.StateStorage;
import com.wrmsr.tokamak.core.driver.state.StateStorageCodec;
import com.wrmsr.tokamak.core.driver.state.StorageState;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PState;
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
    private final SerdeManager serdeManager;
    private final List<AttributesSetCallback> attributesSetCallbacks;
    private final Stat.Updater statUpdater;

    private final List<PNode> prioritizedNodes;
    private final Map<PNode, Integer> prioritiesByNode;

    private final Set<State> allStates;
    private final Map<PNode, Map<Id, State>> statesByIdByNode;
    private final Map<State, State.Mode> modesByState;
    private final Map<State.Mode, SortedMap<Integer, SortedSet<Id>>> idSetsByNodePriorityByMode;
    private final SortedMap<Integer, SortedSet<Id>> pendingInvalidIdSetsByNodePriority;
    private final Set<State> attributesSetCallbackFiredStates;

    public DefaultStateCache(
            Plan plan,
            StateStorage storage,
            SerdeManager serdeManager,
            List<AttributesSetCallback> attributesSetCallbacks,
            Stat.Updater statUpdater)
    {
        this.plan = checkNotNull(plan);
        this.storage = checkNotNull(storage);
        this.serdeManager = checkNotNull(serdeManager);
        this.attributesSetCallbacks = ImmutableList.copyOf(attributesSetCallbacks);
        this.statUpdater = checkNotNull(statUpdater);

        prioritizedNodes = plan.getToposortedNodes().stream().filter(PState.class::isInstance).collect(toImmutableList());
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
    public Optional<State> get(PState node, Id id, EnumSet<GetFlag> flags)
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
            Map<PState, Map<Id, StorageState>> storageResult;
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
            state = serdeManager.getStateStorageCodecsByNode().get(node).decode(storageState);
        }

        checkNotNull(state);
        checkState(state.getMode() == State.Mode.CONSTRUCTING);

        State.Mode initialMode;
        int nodePriority = prioritiesByNode.get(node);
        Set<Id> pendingInvalidationIds = pendingInvalidIdSetsByNodePriority.get(nodePriority);
        boolean isPendingInvalidation = pendingInvalidationIds != null && pendingInvalidationIds.contains(id);
        if (isPendingInvalidation || flags.contains(GetFlag.INVALIDATE)) {
            initialMode = State.Mode.INVALID;
        }
        else {
            switch (state.getStorageMode()) {
                case CREATED:
                    initialMode = State.Mode.INVALID;
                    break;
                case SHARED:
                    initialMode = State.Mode.SHARED;
                    break;
                case EXCLUSIVE:
                    initialMode = State.Mode.EXCLUSIVE;
                    break;
                default:
                    throw new IllegalStateException(state.getMode().toString());
            }
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

        trackStateMode(state);

        state.setModeCallback(this::onStateModeChange);
    }

    private void trackStateMode(State state)
    {
        checkArgument(state.getMode() != State.Mode.CONSTRUCTING);
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

        Map<Integer, SortedSet<Id>> map = idSetsByNodePriorityByMode.computeIfAbsent(newState, s -> new TreeMap<>());
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

    private void onStateModeChange(State state, State.Mode oldMode)
    {
        checkState(state.getMode() != State.Mode.INVALID);
        checkState(state.getMode() != oldMode);

        State.Mode cacheOldMode = checkNotNull(modesByState.get(state));
        checkState(cacheOldMode == oldMode);

        trackStateMode(state);
    }

    @Override
    public void invalidate(PState node, Set<Id> ids)
    {
        throw new IllegalStateException();
    }

    @Override
    public boolean isInvalidated(PState node, Id id)
    {
        checkNotNull(node);
        checkNotNull(id);

        Map<Id, State> map = statesByIdByNode.get(node);
        if (map != null) {
            State state = map.get(id);
            if (state != null && state.getMode() == State.Mode.INVALID) {
                return true;
            }
        }

        // FIXME: assert not both pending and present?
        if (isPendingInvalidation(node, id)) {
            return true;
        }

        return false;
    }

    private boolean isPendingInvalidation(PNode node, Id id)
    {
        SortedSet<Id> set = pendingInvalidIdSetsByNodePriority.get(prioritiesByNode.get(node));
        return set != null && set.contains(id);
    }

    @Override
    public State createPhantom(PState node, Row row)
    {
        return null;
    }

    @Override
    public State setPhantomAttributes(PState node, Row row)
    {
        throw new IllegalStateException();
    }

    @Override
    public Collection<State> getAll()
    {
        return allStates;
    }

    @Override
    public Map<PNode, Set<Id>> getInvalid()
    {
        throw new IllegalStateException();
    }

    @Override
    public Optional<Pair.Immutable<PNode, Id>> getNextInvalid()
    {
        throw new IllegalStateException();
    }

    @Override
    public Map<Id, State> getIdMap(PState node)
    {
        throw new IllegalStateException();
    }

    @Override
    public void flush()
    {
        ImmutableList.Builder<StorageState> storageStates = ImmutableList.builder();
        for (Map.Entry<PNode, Map<Id, State>> e0 : statesByIdByNode.entrySet()) {
            PNode node = e0.getKey();
            Map<Id, State> statesById = e0.getValue();
            StateStorageCodec stateStorageCodec = serdeManager.getStateStorageCodecsByNode().get(node);
            for (Map.Entry<Id, State> e1 : statesById.entrySet()) {
                State state = e1.getValue();
                StorageState storageState = stateStorageCodec.encode(state);
                storageStates.add(storageState);
            }
        }
        try (StateStorage.Context storageContext = storage.createContext()) {
            storage.put(storageContext, storageStates.build(), true);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
