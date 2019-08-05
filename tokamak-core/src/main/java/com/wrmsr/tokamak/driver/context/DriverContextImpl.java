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
import com.wrmsr.tokamak.driver.BuildContext;
import com.wrmsr.tokamak.driver.BuildNodeVisitor;
import com.wrmsr.tokamak.driver.BuildOutput;
import com.wrmsr.tokamak.driver.DriverContext;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.Node;
import org.jdbi.v3.core.Handle;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class DriverContextImpl
        implements DriverContext
{
    private final DriverImpl driver;
    private final Handle jdbiHandle;

    private final RowCache rowCache;
    private final StateCache stateCache;

    public DriverContextImpl(
            DriverImpl driver,
            Handle jdbiHandle)
    {
        this.driver = driver;
        this.jdbiHandle = jdbiHandle;

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

    @Override
    public Handle getJdbiHandle()
    {
        return jdbiHandle;
    }

    @Override
    public List<DriverRow> build(Node node, Key key)
    {
        List<BuildOutput> output = node.accept(
                new BuildNodeVisitor(),
                new BuildContext(
                        this,
                        key));

        checkState(!output.isEmpty());
        return output.stream().map(BuildOutput::getRow).collect(toImmutableList());
    }

    @Override
    public void commit()
    {
        throw new IllegalStateException();
    }
}
