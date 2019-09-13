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
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.func.Signature;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class Catalog
{
    private final Object lock = new Object();

    private final Set<Connector> connectors = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, Connector> connectorsByName = new HashMap<>();

    private final Map<String, Schema> schemasByName = new HashMap<>();

    private final Set<Executor> executors = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, Executor> executorsByName = new HashMap<>();

    private final Map<String, Function> functionsByName = new HashMap<>();

    public Catalog()
    {
    }

    @JsonCreator
    private Catalog(
            @JsonProperty("connectors") List<Connector> connectors,
            @JsonProperty("schemas") List<Schema> schemas,
            @JsonProperty("executors") List<Executor> executors,
            @JsonProperty("functions") List<Function> functions)
    {
        checkNotNull(connectors).forEach(this::addConnector);
        schemas.forEach(s -> {
            checkState(connectors.contains(s.getConnector()));
            checkState(!schemasByName.containsKey(s.getName()));
            s.setCatalog(this);
            schemasByName.put(s.getName(), s);
        });
        checkNotNull(executors).forEach(this::addExecutor);
        functions.forEach(f -> {
            checkState(executors.contains(f.getExecutor()));
            checkState(!schemasByName.containsKey(f.getName()));
            f.setCatalog(this);
            functionsByName.put(f.getName(), f);
        });
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

    @JsonProperty("executors")
    public List<Executor> getExecutors()
    {
        synchronized (lock) {
            return ImmutableList.copyOf(executors);
        }
    }

    public Map<String, Executor> getExecutorsByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(executorsByName);
        }
    }

    @JsonProperty("functions")
    public List<Function> getFunctions()
    {
        synchronized (lock) {
            return ImmutableList.copyOf(functionsByName.values());
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
            connectors.add(connector);
            connectorsByName.put(connector.getName(), connector);
            return connector;
        }
    }

    public Executor addExecutor(Executor executor)
    {
        synchronized (lock) {
            if (executors.contains(executor)) {
                return executor;
            }
            if (functionsByName.get(executor.getName()) != null) {
                throw new IllegalArgumentException("Executor name taken: " + executor.getName());
            }
            executors.add(executor);
            executorsByName.put(executor.getName(), executor);
            return executor;
        }
    }

    public Schema getOrBuildSchema(String name, Connector connector)
    {
        synchronized (lock) {
            Connector existingConnector = connectorsByName.get(connector.getName());
            if (existingConnector == null) {
                addConnector(connector);
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

            schema = new Schema(this, name, connector);

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

    public Function getOrBuildFunction(String name, Executor executor)
    {
        synchronized (lock) {
            Executor existingExecutor = executorsByName.get(executor.getName());
            if (existingExecutor == null) {
                addExecutor(executor);
            }
            else if (existingExecutor != executor) {
                throw new IllegalArgumentException("Executor name taken: " + executor.getName());
            }

            Function function = functionsByName.get(name);
            if (function != null) {
                if (function.getExecutor() != executor) {
                    throw new IllegalArgumentException(
                            String.format("Function %s present under executor %s not %s", name, function.getExecutor(), executor));
                }
                return function;
            }

            Signature signature = checkNotNull(executor.getExecutable(name)).getSignature();

            function = new Function(this, name, signature, executor);

            functionsByName.put(name, function);
            return function;
        }
    }

    public Function lookupFunction(String name)
    {
        synchronized (lock) {
            Function function = functionsByName.get(name);
            if (function == null) {
                throw new IllegalArgumentException("Function not found: " + name);
            }
            return function;
        }
    }
}
