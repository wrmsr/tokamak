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
package com.wrmsr.tokamak.catalog.heap;

import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Connector;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.catalog.heap.table.HeapTable;
import com.wrmsr.tokamak.layout.TableLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class HeapConnector
        implements Connector
{
    private final String name;
    private final Map<SchemaTable, HeapTable> heapTablesBySchemaTable = new HashMap<>();

    public HeapConnector(String name)
    {
        this.name = checkNotNull(name);
    }

    public HeapConnector addTable(HeapTable heapTable)
    {
        checkArgument(!heapTablesBySchemaTable.containsKey(heapTable.getSchemaTable()));
        heapTablesBySchemaTable.put(heapTable.getSchemaTable(), heapTable);
        return this;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Connection connect()
    {
        return new HeapConnection(this);
    }

    @Override
    public TableLayout getTableLayout(SchemaTable schemaTable)
    {
        HeapTable heapTable = checkNotNull(heapTablesBySchemaTable.get(schemaTable));
        return heapTable.getTableLayout();
    }

    @Override
    public Scanner createScanner(Table table, Set<String> fields)
    {
        HeapTable heapTable = checkNotNull(heapTablesBySchemaTable.get(table.getSchemaTable()));
        return new HeapScanner(this, heapTable, fields);
    }
}
