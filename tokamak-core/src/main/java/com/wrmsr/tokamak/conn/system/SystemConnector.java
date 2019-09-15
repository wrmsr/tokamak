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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Connector;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.layout.TableLayout;

import java.util.Map;
import java.util.Set;

public final class SystemConnector
        implements Connector
{
    @Override
    public String getName()
    {
        return "system";
    }

    @Override
    public Connection connect()
    {
        return new SystemConnection(this);
    }

    @Override
    public Map<String, Set<String>> getSchemaTables()
    {
        return ImmutableMap.of();
    }

    @Override
    public TableLayout getTableLayout(SchemaTable schemaTable)
    {
        throw new IllegalStateException();
    }

    @Override
    public Scanner createScanner(Table table, Set<String> fields)
    {
        throw new IllegalStateException();
    }
}
