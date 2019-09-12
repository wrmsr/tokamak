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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.func.Function;

import java.util.HashMap;
import java.util.Map;

public final class Catalog
{
    private final Object lock = new Object();
    private final Map<String, Connector> connectorsByName = new HashMap<>();
    private final Map<String, Schema> schemasByName = new HashMap<>();
    private final Map<String, Function> functionsByName = new HashMap<>();

    public Map<String, Connector> getConnectorsByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(connectorsByName);
        }
    }

    public Map<String, Schema> getSchemasByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(schemasByName);
        }
    }

    public Map<String, Function> getFunctionsByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(functionsByName);
        }
    }

    public Schema getOrBuildSchema(String name, Connector connector)
    {
        synchronized (lock) {
            Connector existingConnector = connectorsByName.get(connector.getName());
            if (existingConnector == null) {
                connectorsByName.put(connector.getName(), connector);
            }
            else if (existingConnector != connector) {
                throw new IllegalArgumentException("Connector name taken: " + connector.getName());
            }

            return schemasByName.computeIfAbsent(name, n ->
                    new Schema(
                            this,
                            name,
                            connector)
            );
        }
    }

    public Table lookupSchemaTable(SchemaTable schemaTable)
    {
        synchronized (lock) {
            Schema schema = schemasByName.get(schemaTable.getSchema());
            if (schema == null) {
                throw new IllegalArgumentException("Schema not found: " + schemaTable);
            }
            Table table = schema.getTablesByName().get(schemaTable.getTable());
            if (table == null) {
                throw new IllegalArgumentException("Table not found: " + schemaTable);
            }
            return table;
        }
    }

    public Function addFunction(Function function)
    {
        synchronized (lock) {
            if (functionsByName.get(function.getName()) != null) {
                throw new IllegalArgumentException("Function name taken: " + function.getName());
            }
            functionsByName.put(function.getName(), function);
            return function;
        }
    }
}
