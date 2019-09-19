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
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Connector;
import com.wrmsr.tokamak.driver.Driver;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.build.Builder;
import com.wrmsr.tokamak.driver.context.diag.JournalEntry;
import com.wrmsr.tokamak.driver.context.diag.Stat;
import com.wrmsr.tokamak.driver.context.state.StateCache;
import com.wrmsr.tokamak.driver.context.state.DefaultStateCache;
import com.wrmsr.tokamak.driver.state.State;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public class DriverContextImpl
        implements Driver.Context
{
    private final DriverImpl driver;

    private final DefaultStateCache stateCache;
    private final InvalidationManager invalidationManager;
    private final LinkageManager linkageManager;

    private final boolean journaling;
    private final List<JournalEntry> journalEntries;

    public DriverContextImpl(
            DriverImpl driver)
    {
        this.driver = driver;

        this.stateCache = new DefaultStateCache(
                driver.getPlan(),
                driver.getStateStorage(),
                driver.getSerdeManager(),
                ImmutableList.of(this::onStateAttributesSet),
                Stat.Updater.nop());

        this.invalidationManager = new InvalidationManager();

        this.linkageManager = new LinkageManager();

        journaling = false;
        journalEntries = null;
    }

    protected void addJournalEntry(JournalEntry entry)
    {
        if (!journaling) {
            return;
        }
        checkNotNull(journalEntries);
        journalEntries.add(entry);
    }

    @Override
    public DriverImpl getDriver()
    {
        return driver;
    }

    private final Map<Connector, Connection> connectionsByConnection = new HashMap<>();

    @Override
    public Connection getConnection(Connector connector)
    {
        return connectionsByConnection.computeIfAbsent(connector, c -> connector.connect());
    }

    protected void onStateAttributesSet(State state)
    {
        DriverRow row = new DriverRow(
                state.getNode(),
                driver.getLineagePolicy().build(),
                state.getId(),
                state.getAttributes());

        if (state.getMode() == State.Mode.MODIFIED) {
            invalidationManager.invalidate(state);
        }
    }

    protected Optional<Collection<State>> getStateCached(StatefulNode node, Key key)
    {
        Id id;
        if (key instanceof IdKey) {
            id = ((IdKey) key).getId();
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            // if (!node.getIdFieldSets().contains(fieldKey.getFields())) {
            //     return Optional.empty();
            // }
            // id =
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked"})
    public Collection<DriverRow> build(Builder builder, Key key)
    {
        Node node = builder.getNode();
        if (journaling) {
            addJournalEntry(new JournalEntry.BuildInput(node, key));
        }

        if (builder.getNode() instanceof StatefulNode && key instanceof IdKey) {
            StatefulNode statefulNode = (StatefulNode) builder.getNode();
            IdKey idKey = (IdKey) key;
            Optional<State> stateOpt = stateCache.get(statefulNode, idKey.getId(), EnumSet.of(StateCache.GetFlag.CREATE));
            if (stateOpt.isPresent()) {
                State state = stateOpt.get();
                checkState(state.getId().equals(idKey.getId()));
                checkState(!state.getMode().isStorageMode());
                if (state.getMode() != State.Mode.INVALID) {
                    DriverRow row = new DriverRow(
                            statefulNode,
                            driver.getLineagePolicy().build(),
                            state.getId(),
                            state.getAttributes());
                    if (journaling) {
                        addJournalEntry(new JournalEntry.StateCachedBuildOutput(node, key, ImmutableList.of(row), state));
                    }
                }
            }
        }

        Collection<DriverRow> rows = builder.build(this, key);
        checkNotEmpty(rows);
        if (journaling) {
            addJournalEntry(new JournalEntry.UncachedBuildOutput(node, key, rows));
        }

        if (node instanceof StatefulNode) {
            StatefulNode statefulNode = (StatefulNode) node;
            for (DriverRow row : rows) {
                if (row.getId() == null) {
                    checkState(row.isNull());
                    continue;
                }

                Optional<State> stateOpt = stateCache.get(statefulNode, row.getId(), EnumSet.of(StateCache.GetFlag.CREATE));
                State state = stateOpt.get();
                if (state.getMode() == State.Mode.INVALID) {
                    state.setAttributes(row.getAttributes());
                }
            }
        }

        return rows;
    }

    public Collection<DriverRow> build(Node node, Key key)
    {
        Builder builder = checkNotNull(driver.getBuildersByNode().get(node));
        return build(builder, key);
    }

    @Override
    public void commit()
    {
        stateCache.flush();
    }

    @Override
    public void close()
    {
        for (Connection connection : connectionsByConnection.values()) {
            connection.close();
        }
    }
}
