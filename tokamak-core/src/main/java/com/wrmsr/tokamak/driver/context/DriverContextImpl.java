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
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Connector;
import com.wrmsr.tokamak.driver.BuildNodeVisitor;
import com.wrmsr.tokamak.driver.DriverContext;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class DriverContextImpl
        implements DriverContext
{
    private final DriverImpl driver;

    private final RowCache rowCache;
    private final StateCache stateCache;

    public DriverContextImpl(
            DriverImpl driver)
    {
        this.driver = driver;

        this.rowCache = new RowCache();
        this.stateCache = new StateCache(
                driver.getPlan(),
                driver.getStateStorage(),
                ImmutableList.of(),
                Stat.Updater.nop());
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

    public List<DriverRow> build(Node node, Key key)
    {
        List<DriverRow> output = node.accept(
                new BuildNodeVisitor(this),
                key);
        return checkNotEmpty(output);
    }

    @Override
    public void commit()
    {
        throw new IllegalStateException();
    }

    @Override
    public void close()
            throws Exception
    {
        for (Connection connection : connectionsByConnection.values()) {
            connection.close();
        }
    }
}
