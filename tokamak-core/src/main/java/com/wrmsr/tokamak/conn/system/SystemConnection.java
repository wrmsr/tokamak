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
package com.wrmsr.tokamak.conn.system;

import com.wrmsr.tokamak.catalog.Connection;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SystemConnection
        implements Connection
{
    private final SystemConnector systemConnector;

    public SystemConnection(SystemConnector systemConnector)
    {
        this.systemConnector = checkNotNull(systemConnector);
    }

    public SystemConnector getSystemConnector()
    {
        return systemConnector;
    }

    @Override
    public void close()
    {
    }
}
