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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.catalog.Connection;
import com.wrmsr.tokamak.core.catalog.Connector;
import com.wrmsr.tokamak.core.catalog.Schema;
import com.wrmsr.tokamak.core.driver.Driver;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.BuilderContext;
import com.wrmsr.tokamak.core.driver.build.ContextualBuilder;
import com.wrmsr.tokamak.core.driver.build.ops.GetStateBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ScanBuildOp;
import com.wrmsr.tokamak.core.driver.context.diag.JournalEntry;
import com.wrmsr.tokamak.core.driver.context.diag.Stat;
import com.wrmsr.tokamak.core.driver.context.state.DefaultStateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.util.Cell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreCollectors.toLinkedMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.UnaryOperator.identity;

public class DriverContextImpl
        implements Driver.Context
{
    private final DriverImpl driver;

    private final DefaultStateCache stateCache;
    private final InvalidationManager invalidationManager;
    private final LinkageManager linkageManager;

    private final boolean journaling;
    private final List<JournalEntry> journalEntries;

    private final Map<ContextualBuilder<?>, BuilderContext> builderContextMap;

    private Driver.ContextState state;

    public DriverContextImpl(
            DriverImpl driver)
    {
        this.driver = checkNotNull(driver);

        this.stateCache = new DefaultStateCache(
                driver.getPlan(),
                driver.getStateStorage(),
                driver.getSerdeManager(),
                ImmutableList.of(this::onStateAttributesSet),
                Stat.Updater.nop());

        this.invalidationManager = new InvalidationManager(
                driver.getPlan(),
                stateCache);

        this.linkageManager = new LinkageManager(stateCache);

        // journaling = false;
        // journalEntries = null;
        journaling = true;
        journalEntries = new ArrayList<>();

        state = Driver.ContextState.ACTIVE;

        builderContextMap = driver.getContextualBuilders().stream()
                .collect(toImmutableMap(identity(), cb -> cb.buildContext(this)));
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

    @Override
    public Driver.ContextState getState()
    {
        return state;
    }

    public DefaultStateCache getStateCache()
    {
        return stateCache;
    }

    public InvalidationManager getInvalidationManager()
    {
        return invalidationManager;
    }

    public LinkageManager getLinkageManager()
    {
        return linkageManager;
    }

    private final Map<Connector, Connection> connectionsByConnection = new HashMap<>();

    @Override
    public Connection getConnection(Connector connector)
    {
        return connectionsByConnection.computeIfAbsent(connector, c -> connector.connect());
    }

    protected void onStateAttributesSet(State state)
    {
        checkState(this.state == Driver.ContextState.ACTIVE);

        if (state.getMode() == State.Mode.MODIFIED) {
            invalidationManager.invalidate(state);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T extends BuilderContext> T getBuildContext(ContextualBuilder<?> contextualBuilder)
    {
        return (T) checkNotNull(builderContextMap.get(contextualBuilder));
    }

    @SuppressWarnings({"unchecked"})
    public Collection<DriverRow> buildSync(Builder<?> builder, Key key)
    {
        checkState(state == Driver.ContextState.ACTIVE);

        PNode node = builder.getNode();
        if (journaling) {
            addJournalEntry(new JournalEntry.BuildInput(node, key));
        }

        Cell<Collection<DriverRow>> rowsCell = Cell.setOnce();

        builder.build(this, key, op -> {
            checkState(op.getOrigin() == builder);
            if (journaling) {
                addJournalEntry(new JournalEntry.ContextBuildOp(op));
            }

            if (op instanceof GetStateBuildOp) {
                GetStateBuildOp gsop = (GetStateBuildOp) op;
                ImmutableMap.Builder<Id, State> mapBuilder = ImmutableMap.builder();
                gsop.getIds().forEach(id -> {
                    Optional<State> state = stateCache.get(gsop.getNode(), id, gsop.getFlags());
                    state.ifPresent(st -> mapBuilder.put(st.getId(), st));
                });
                Map<Id, State> map = mapBuilder.build();

                if (journaling) {
                    addJournalEntry(new JournalEntry.ContextBuildOpCallback(op, map));
                }
                gsop.getCallback().accept(map);
            }

            else if (op instanceof RequestBuildOp) {
                RequestBuildOp rop = (RequestBuildOp) op;
                ImmutableMap.Builder<Builder<?>, Map<Key, List<DriverRow>>> outBuilder = ImmutableMap.builder();
                rop.getKeysSetsByBuilder().forEach((kb, ks) -> {
                    ImmutableMap.Builder<Key, List<DriverRow>> boutBuilder = ImmutableMap.builder();
                    ks.forEach(k -> {
                        boutBuilder.put(k, ImmutableList.copyOf(buildSync(kb, k)));
                    });
                    outBuilder.put(kb, boutBuilder.build());
                });
                Map<Builder<?>, Map<Key, List<DriverRow>>> out = outBuilder.build();

                if (journaling) {
                    addJournalEntry(new JournalEntry.ContextBuildOpCallback(op, out));
                }
                rop.getCallback().accept(out);
            }

            else if (op instanceof ResponseBuildOp) {
                ResponseBuildOp rop = (ResponseBuildOp) op;
                checkState(rop.getKey().equals(key));

                if (journaling) {
                    addJournalEntry(new JournalEntry.ContextBuildOpCallback(op, rop.getRows()));
                }
                rowsCell.set(rop.getRows());
            }

            else if (op instanceof ScanBuildOp) {
                ScanBuildOp sop = (ScanBuildOp) op;
                Schema schema = driver.getCatalog().getSchemasByName().get(((PScan) builder.getNode()).getSchemaTable().getSchema());
                Connection connection = getConnection(schema.getConnector());
                List<Map<String, Object>> scanRows = sop.getScanner().scan(connection, key);
                List<Map<String, Object>> orderedScanRows = scanRows.stream().map(r ->
                        builder.getNode().getFields().stream().map(Field::getName).collect(toLinkedMap(identity(), r::get)))
                        .collect(toImmutableList());

                if (journaling) {
                    addJournalEntry(new JournalEntry.ContextBuildOpCallback(op, orderedScanRows));
                }
                sop.getCallback().accept(orderedScanRows);
            }

            else {
                throw new IllegalStateException(Objects.toString(op));
            }
        });

        Collection<DriverRow> rows = rowsCell.get();
        checkNotEmpty(rows);
        if (journaling) {
            addJournalEntry(new JournalEntry.BuildOutput(node, key, rows));
        }

        return rows;
    }

    public Collection<DriverRow> buildSync(PNode node, Key key)
    {
        Builder<?> builder = checkNotNull(driver.getBuildersByNode().get(node));
        return buildSync(builder, key);
    }

    @Override
    public void commit()
    {
        checkState(state == Driver.ContextState.ACTIVE);
        try {
            linkageManager.update();
            stateCache.flush();
            state = Driver.ContextState.COMMITTED;
        }
        catch (Exception e) {
            abort();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void abort()
    {
        checkState(state == Driver.ContextState.ACTIVE);

        state = Driver.ContextState.ABORTED;
    }

    @Override
    public void close()
    {
        if (state == Driver.ContextState.ACTIVE) {
            abort();
        }

        for (Connection connection : connectionsByConnection.values()) {
            connection.close();
        }
    }
}
