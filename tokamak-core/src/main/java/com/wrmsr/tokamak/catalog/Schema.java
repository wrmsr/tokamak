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
package com.wrmsr.tokamak.catalog;

import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.layout.TableLayout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Schema
{
    private final Catalog catalog;
    private final String name;
    private final Connector connector;

    private final Map<String, Table> tablesByName = new HashMap<>();

    public Schema(Catalog catalog, String name, Connector connector)
    {
        this.catalog = checkNotNull(catalog);
        this.name = checkNotNull(name);
        this.connector = checkNotNull(connector);
    }

    @Override
    public String toString()
    {
        return "Schema{" +
                "name='" + name + '\'' +
                ", connector=" + connector +
                '}';
    }

    public Catalog getCatalog()
    {
        return catalog;
    }

    public String getName()
    {
        return name;
    }

    public Connector getConnector()
    {
        return connector;
    }

    public Map<String, Table> getTablesByName()
    {
        return Collections.unmodifiableMap(tablesByName);
    }

    public Table getOrBuildTable(String name)
    {
        return tablesByName.computeIfAbsent(name, n -> {
            TableLayout layout = connector.getTableLayout(SchemaTable.of(this.name, name));
            return new Table(
                    this,
                    name,
                    layout);
        });
    }
}