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
package com.wrmsr.tokamak.driver;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.driver.build.Builder;
import com.wrmsr.tokamak.driver.build.BuilderFactory;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.conn.heap.MapHeapStateStorage;
import com.wrmsr.tokamak.driver.state.StateStorage;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public class DriverImpl
        implements Driver
{
    private final Catalog catalog;
    private final Plan plan;

    private final CodecManager codecManager;
    private final StateStorage stateStorage;

    public DriverImpl(Catalog catalog, Plan plan)
    {
        this.catalog = checkNotNull(catalog);
        this.plan = checkNotNull(plan);

        codecManager = new CodecManager(plan);
        stateStorage = new MapHeapStateStorage();
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
        return LineagePolicy.MINIMAL;
    }

    public CodecManager getCodecManager()
    {
        return codecManager;
    }

    public StateStorage getStateStorage()
    {
        return stateStorage;
    }

    private final SupplierLazyValue<Map<ScanNode, Scanner>> scannersByScanNode = new SupplierLazyValue<>();

    public Map<ScanNode, Scanner> getScannersByNode()
    {
        return scannersByScanNode.get(() ->
                plan.getNodeTypeList(ScanNode.class).stream()
                        .collect(toImmutableMap(identity(), scanNode -> {
                            Table table = catalog.getSchemaTable(scanNode.getSchemaTable());
                            return table.getSchema().getConnector().createScanner(table, scanNode.getFields().keySet());
                        })));
    }

    private final SupplierLazyValue<Map<Node, Builder>> buildersByNode = new SupplierLazyValue<>();

    public Map<Node, Builder> getBuildersByNode()
    {
        return buildersByNode.get(() -> {
            BuilderFactory factory = new BuilderFactory(this);
            return plan.getToposortedNodes().stream()
                    .collect(toImmutableMap(identity(), factory::get));
        });
    }

    @Override
    public Context createContext()
    {
        return new DriverContextImpl(this);
    }

    @Override
    public Collection<Row> build(Context context, Node node, Key key)
            throws IOException
    {
        checkNotNull(node);
        checkNotNull(key);
        DriverContextImpl driverContextImpl = checkNotNull((DriverContextImpl) context);
        return (Collection) driverContextImpl.build(node, key);
    }

    @Override
    public Collection<Row> sync(Context context, Map<Node, Set<Id>> idSetsByNode)
            throws IOException
    {
        checkNotNull(idSetsByNode);
        DriverContextImpl driverContextImpl = checkNotNull((DriverContextImpl) context);
        throw new IllegalStateException();
    }
}
