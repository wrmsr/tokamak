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
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ScanBuildOp;
import com.wrmsr.tokamak.core.driver.context.diag.JournalEntry;
import com.wrmsr.tokamak.core.driver.context.diag.Stat;
import com.wrmsr.tokamak.core.driver.context.state.DefaultStateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.ScanNode;
import com.wrmsr.tokamak.util.Cell;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
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

    private final Map<ContextualBuilder, BuilderContext> builderContextMap;

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

        this.linkageManager = new LinkageManager(stateCache);

        journaling = false;
        journalEntries = null;

        builderContextMap = driver.getContextualBuilders().stream().collect(toImmutableMap(identity(), cb -> cb.buildContext(this)));
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
        if (state.getMode() == State.Mode.MODIFIED) {
            invalidationManager.invalidate(state);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T extends BuilderContext> T getBuildContext(ContextualBuilder contextualBuilder)
    {
        return (T) checkNotNull(builderContextMap.get(contextualBuilder));
    }

    @SuppressWarnings({"unchecked"})
    public Collection<DriverRow> buildSync(Builder<?> builder, Key key)
    {
        Node node = builder.getNode();
        if (journaling) {
            addJournalEntry(new JournalEntry.BuildInput(node, key));
        }

        Cell<Collection<DriverRow>> rowsCell = Cell.setOnce();

        builder.build(this, key, op -> {
            checkState(op.getOrigin() == builder);
            if (op instanceof RequestBuildOp) {
                RequestBuildOp rop = (RequestBuildOp) op;
                Collection<DriverRow> rows = buildSync(rop.getBuilder(), rop.getKey());
                rop.getCallback().accept(rows);
            }
            else if (op instanceof ResponseBuildOp) {
                ResponseBuildOp rop = (ResponseBuildOp) op;
                checkState(rop.getKey().equals(key));
                rowsCell.set(rop.getRows());
            }
            else if (op instanceof ScanBuildOp) {
                ScanBuildOp sop = (ScanBuildOp) op;
                Schema schema = driver.getCatalog().getSchemasByName().get(((ScanNode) builder.getNode()).getSchemaTable().getSchema());
                Connection connection = getConnection(schema.getConnector());
                List<Map<String, Object>> scanRows = sop.getScanner().scan(connection, key);
                sop.getCallback().accept(scanRows);
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

    public Collection<DriverRow> buildSync(Node node, Key key)
    {
        Builder builder = checkNotNull(driver.getBuildersByNode().get(node));
        return buildSync(builder, key);
    }

    @Override
    public void commit()
    {
        linkageManager.update();
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
