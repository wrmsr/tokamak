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
package com.wrmsr.tokamak.core.driver;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Scanner;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.conn.heap.MapHeapStateStorage;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.BuilderFactory;
import com.wrmsr.tokamak.core.driver.build.ContextualBuilder;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.driver.context.lineage.LineageGranularity;
import com.wrmsr.tokamak.core.driver.context.lineage.LineagePolicy;
import com.wrmsr.tokamak.core.driver.context.lineage.LineagePolicyImpl;
import com.wrmsr.tokamak.core.driver.context.lineage.LineageRetention;
import com.wrmsr.tokamak.core.driver.state.StateStorage;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public class DriverImpl
        implements Driver
{
    private final Catalog catalog;
    private final Plan plan;

    private final SerdeManager serdeManager;
    private final StateStorage stateStorage;
    private final LineagePolicy lineagePolicy;

    public DriverImpl(
            Catalog catalog,
            Plan plan,
            Optional<SerdeManager> serdeManager,
            Optional<StateStorage> stateStorage,
            Optional<LineagePolicy> lineagePolicy)
    {
        this.catalog = checkNotNull(catalog);
        this.plan = checkNotNull(plan);

        this.serdeManager = serdeManager.orElseGet(() -> new SerdeManager(plan));
        this.stateStorage = stateStorage.orElseGet(() -> new MapHeapStateStorage());
        this.lineagePolicy = lineagePolicy.orElseGet(() -> new LineagePolicyImpl(LineageRetention.MINIMAL, LineageGranularity.ID));
    }

    public DriverImpl(Catalog catalog, Plan plan)
    {
        this(
                catalog,
                plan,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    @Override
    public Plan getPlan()
    {
        return plan;
    }

    @Override
    public Catalog getCatalog()
    {
        return catalog;
    }

    public LineagePolicy getLineagePolicy()
    {
        return lineagePolicy;
    }

    public SerdeManager getSerdeManager()
    {
        return serdeManager;
    }

    public StateStorage getStateStorage()
    {
        return stateStorage;
    }

    private final SupplierLazyValue<Map<PScan, Scanner>> scannersByScanNode = new SupplierLazyValue<>();

    public Map<PScan, Scanner> getScannersByNode()
    {
        return scannersByScanNode.get(() ->
                plan.getNodeTypeList(PScan.class).stream()
                        .collect(toImmutableMap(identity(), scanNode -> {
                            Table table = catalog.getSchemaTable(scanNode.getSchemaTable());
                            return table.getSchema().getConnector().newScanner(table, scanNode.getFields().getNames());
                        })));
    }

    private final SupplierLazyValue<Map<PNode, Builder<?>>> buildersByNode = new SupplierLazyValue<>();

    public Map<PNode, Builder<?>> getBuildersByNode()
    {
        return buildersByNode.get(() -> {
            BuilderFactory factory = new BuilderFactory(this);
            return plan.getToposortedNodes().stream()
                    .collect(toImmutableMap(identity(), factory::get));
        });
    }

    private final SupplierLazyValue<List<ContextualBuilder<?>>> contextualBuilders = new SupplierLazyValue<>();

    public List<ContextualBuilder<?>> getContextualBuilders()
    {
        return contextualBuilders.get(() -> checkOrdered(getBuildersByNode()).values().stream()
                .filter(ContextualBuilder.class::isInstance)
                .map(cb -> (ContextualBuilder<?>) cb)
                .collect(toImmutableList()));
    }

    @Override
    public Context newContext()
    {
        return new DriverContextImpl(this);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Collection<Row> build(Context context, PNode node, Key key)
            throws IOException
    {
        checkNotNull(node);
        checkNotNull(key);
        DriverContextImpl driverContextImpl = checkNotNull((DriverContextImpl) context);
        return (Collection) driverContextImpl.buildSync(node, key);
    }

    @Override
    public Collection<Row> sync(Context context, Map<PNode, Set<Id>> idSetsByNode)
            throws IOException
    {
        checkNotNull(idSetsByNode);
        DriverContextImpl driverContextImpl = checkNotNull((DriverContextImpl) context);
        throw new IllegalStateException();
    }
}
