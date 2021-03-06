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
package com.wrmsr.tokamak.core.conn;

import com.wrmsr.tokamak.core.catalog.CatalogRegistry;
import com.wrmsr.tokamak.core.catalog.ConnectorType;
import com.wrmsr.tokamak.core.conn.heap.HeapConnector;
import com.wrmsr.tokamak.core.conn.jdbc.JdbcConnector;
import com.wrmsr.tokamak.core.conn.kv.KvConnector;
import com.wrmsr.tokamak.core.conn.system.SystemConnector;

public final class BuiltinConnectors
{
    /*
    TODO:
     - RemappingConnector? alt: all in on views?
    */

    private BuiltinConnectors()
    {
    }

    public static CatalogRegistry register(CatalogRegistry registry)
    {
        registry.register(new ConnectorType("heap", HeapConnector.class));
        registry.register(new ConnectorType("jdbc", JdbcConnector.class));
        registry.register(new ConnectorType("kv", KvConnector.class));
        registry.register(new ConnectorType("system", SystemConnector.class));
        return registry;
    }
}
