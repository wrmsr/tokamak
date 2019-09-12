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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.func.Function;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Catalog
{
    private final Object lock = new Object();

    private final Set<Connector> connectors = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, Connector> connectorsByName = new HashMap<>();

    private final Map<String, Schema> schemasByName = new HashMap<>();

    private final Set<Function> functions = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, Function> functionsByName = new HashMap<>();

    @JsonCreator
    public Catalog(
            @JsonProperty("connectors") List<Connector> connectors,
            @JsonProperty("schemas") List<Schema> schemas,
            @JsonProperty("functions") List<Function> functions)
    {
        checkNotNull(connectors).forEach(this::addConnector);
        // FIXME:
        // connectorSchemaTables.forEach((cn, sts) -> {
        //     Connector c = checkNotNull(connectorsByName.get(cn));
        //     sns.forEach(sn -> getOrBuildSchema(sn, c));
        // });
        checkNotNull(functions).forEach(this::addFunction);
    }

    public Catalog()
    {
    }

    @JsonProperty("connectors")
    public List<Connector> getConnectors()
    {
        synchronized (lock) {
            return ImmutableList.copyOf(connectors);
        }
    }

    public Map<String, Connector> getConnectorsByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(connectorsByName);
        }
    }

    @JsonProperty("schemas")
    public List<Schema> getSchemas()
    {
        synchronized (lock) {
            return ImmutableList.copyOf(schemasByName.values());
        }
    }

    public Map<String, Schema> getSchemasByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(schemasByName);
        }
    }

    @JsonProperty("functions")
    public List<Function> getFunctions()
    {
        synchronized (lock) {
            return ImmutableList.copyOf(functions);
        }
    }

    public Map<String, Function> getFunctionsByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(functionsByName);
        }
    }

    public Connector addConnector(Connector connector)
    {
        synchronized (lock) {
            if (connectors.contains(connector)) {
                return connector;
            }
            if (functionsByName.get(connector.getName()) != null) {
                throw new IllegalArgumentException("Connector name taken: " + connector.getName());
            }
            connectorsByName.put(connector.getName(), connector);
            return connector;
        }
    }

    public Schema getOrBuildSchema(Connector connector, String name)
    {
        synchronized (lock) {
            Connector existingConnector = connectorsByName.get(connector.getName());
            if (existingConnector == null) {
                connectorsByName.put(connector.getName(), connector);
            }
            else if (existingConnector != connector) {
                throw new IllegalArgumentException("Connector name taken: " + connector.getName());
            }

            Schema schema = schemasByName.get(name);
            if (schema != null) {
                if (schema.getConnector() != connector) {
                    throw new IllegalArgumentException(
                            String.format("Schema %s present under connector %s not %s", name, schema.getConnector(), connector));
                }
                return schema;
            }

            schema = new Schema(
                    this,
                    connector,
                    name);
            schemasByName.put(name, schema);
            return schema;
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
            if (functions.contains(function)) {
                return function;
            }
            if (functionsByName.get(function.getName()) != null) {
                throw new IllegalArgumentException("Function name taken: " + function.getName());
            }
            functionsByName.put(function.getName(), function);
            return function;
        }
    }
}
