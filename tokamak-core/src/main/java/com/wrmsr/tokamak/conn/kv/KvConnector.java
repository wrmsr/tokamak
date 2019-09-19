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
package com.wrmsr.tokamak.conn.kv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Connector;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.conn.heap.HeapScanner;
import com.wrmsr.tokamak.conn.heap.table.HeapTable;
import com.wrmsr.tokamak.layout.TableLayout;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class KvConnector
        implements Connector
{
    private final String name;

    private final Object lock = new Object();
    private volatile Map<SchemaTable, KvTable> tablesBySchemaTable = ImmutableMap.of();

    @JsonCreator
    public KvConnector(
            @JsonProperty("name") String name,
            @JsonProperty("tables") Iterable<KvTable> tables)
    {
        this.name = checkNotEmpty(name);
        tables.forEach(this::addTable);
    }

    public KvConnector addTable(KvTable heapTable)
    {
        synchronized (lock) {
            checkArgument(!tablesBySchemaTable.containsKey(heapTable.getSchemaTable()));
            tablesBySchemaTable = ImmutableMap.<SchemaTable, KvTable>builder()
                    .putAll(tablesBySchemaTable)
                    .put(heapTable.getSchemaTable(), heapTable)
                    .build();
            return this;
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @JsonProperty("tables")
    public List<KvTable> getTables()
    {
        return ImmutableList.copyOf(tablesBySchemaTable.values());
    }

    @Override
    public Connection connect()
    {
        return new KvConnection(this);
    }

    @Override
    public Map<String, Set<String>> getSchemaTables()
    {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        tablesBySchemaTable.keySet().forEach(st -> map.computeIfAbsent(st.getSchema(), s -> new LinkedHashSet<>()).add(st.getTable()));
        return map.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> ImmutableSet.copyOf(e.getValue())));
    }

    @Override
    public TableLayout getTableLayout(SchemaTable schemaTable)
    {
        return checkNotNull(tablesBySchemaTable.get(schemaTable)).getTableLayout();
    }

    @Override
    public Scanner createScanner(Table table, Set<String> fields)
    {
        return new KvScanner(this, checkNotNull(tablesBySchemaTable.get(table.getSchemaTable())), fields);
    }
}
