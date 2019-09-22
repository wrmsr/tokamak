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
package com.wrmsr.tokamak.core.catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.layout.TableLayout;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class Schema
{
    private final String name;
    private final Connector connector;

    private Catalog catalog;

    private final Object lock = new Object();

    private volatile Map<String, Table> tablesByName;

    public Schema(Catalog catalog, String name, Connector connector)
    {
        this.catalog = checkNotNull(catalog);
        this.name = checkNotEmpty(name);
        this.connector = checkNotNull(connector);
        tablesByName = ImmutableMap.of();
    }

    @JsonCreator
    private Schema(
            @JsonProperty("name") String name,
            @JsonProperty("connector") Connector connector,
            @JsonProperty("tables") List<Table> tables)
    {
        this.name = checkNotNull(name);
        this.connector = checkNotNull(connector);
        ImmutableMap.Builder<String, Table> tablesByName = ImmutableMap.builderWithExpectedSize(tables.size());
        tables.forEach(t -> {
            t.setSchema(this);
            tablesByName.put(t.getName(), t);
        });
        this.tablesByName = tablesByName.build();
    }

    void setCatalog(Catalog catalog)
    {
        checkState(this.catalog == null);
        this.catalog = checkNotNull(catalog);
    }

    @Override
    public String toString()
    {
        return "Schema{" +
                "name='" + name + '\'' +
                ", connector=" + connector +
                '}';
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("connector")
    public Connector getConnector()
    {
        return connector;
    }

    @JsonProperty("tables")
    public Collection<Table> getTables()
    {
        return tablesByName.values();
    }

    public Map<String, Table> getTablesByName()
    {
        return tablesByName;
    }

    public Table addTable(String name)
    {
        synchronized (lock) {
            Table table = tablesByName.get(name);
            if (table != null) {
                throw new IllegalArgumentException("Table name taken: " + name);
            }
            TableLayout layout = connector.getTableLayout(SchemaTable.of(this.name, name));
            table = new Table(this, name, layout);
            tablesByName = ImmutableMap.<String, Table>builder().putAll(tablesByName).put(name, table).build();
            return table;
        }
    }

    public Table getTable(String name)
    {
        Table table = tablesByName.get(name);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + name);
        }
        return table;
    }
}
