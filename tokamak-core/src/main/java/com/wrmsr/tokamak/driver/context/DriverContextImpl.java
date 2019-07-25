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

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.driver.DriverContext;
import com.wrmsr.tokamak.node.Node;
import org.jdbi.v3.core.Handle;

import java.util.List;

public final class DriverContextImpl
        implements DriverContext
{
    private final Handle jdbiHandle;

    public DriverContextImpl(Handle jdbiHandle)
    {
        this.jdbiHandle = jdbiHandle;
    }

    @Override
    public Handle getJdbiHandle()
    {
        return jdbiHandle;
    }

    @Override
    public List<Row> build(Node node, Key key)
    {
        throw new IllegalStateException();
    }

    @Override
    public void commit()
    {
        throw new IllegalStateException();
    }
}
