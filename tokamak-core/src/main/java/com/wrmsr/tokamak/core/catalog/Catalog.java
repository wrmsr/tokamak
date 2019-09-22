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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.exec.Signature;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class Catalog
{
    private final List<Catalog> parents;

    private final Object lock = new Object();

    private volatile Map<String, Connector> connectorsByName = ImmutableMap.of();
    private volatile Map<String, Schema> schemasByName = ImmutableMap.of();
    private volatile Map<String, Executor> executorsByName = ImmutableMap.of();
    private volatile Map<String, Function> functionsByName = ImmutableMap.of();

    public Catalog(List<Catalog> parent)
    {
        this.parents = ImmutableList.copyOf(parent);
    }

    public Catalog()
    {
        this(ImmutableList.of());
    }

    @JsonCreator
    private Catalog(
            @JsonProperty("parent") List<Catalog> parent,
            @JsonProperty("connectors") List<Connector> connectors,
            @JsonProperty("schemas") List<Schema> schemas,
            @JsonProperty("executors") List<Executor> executors,
            @JsonProperty("functions") List<Function> functions)
    {
        this.parents = ImmutableList.copyOf(parent);
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

    @JsonProperty("parent")
    public List<Catalog> getParents()
    {
        return parents;
    }

    @JsonProperty("connectors")
    public Collection<Connector> getConnectors()
    {
        return connectorsByName.values();
    }

    public Map<String, Connector> getConnectorsByName()
    {
        return connectorsByName;
    }

    @JsonProperty("schemas")
    public Collection<Schema> getSchemas()
    {
        return schemasByName.values();
    }

    public Map<String, Schema> getSchemasByName()
    {
        return schemasByName;
    }

    @JsonProperty("executors")
    public Collection<Executor> getExecutors()
    {
        return executorsByName.values();
    }

    public Map<String, Executor> getExecutorsByName()
    {
        return executorsByName;
    }

    @JsonProperty("functions")
    public Collection<Function> getFunctions()
    {
        return functionsByName.values();
    }

    public Map<String, Function> getFunctionsByName()
    {
        return functionsByName;
    }

    public <T extends Connector> T addConnector(T connector)
    {
        synchronized (lock) {
            if (connectorsByName.get(connector.getName()) != null) {
                throw new IllegalArgumentException("Connector name taken: " + connector.getName());
            }
            connectorsByName = ImmutableMap.<String, Connector>builder().putAll(connectorsByName).put(connector.getName(), connector).build();
            return connector;
        }
    }

    public Schema addSchema(String name, Connector connector)
    {
        synchronized (lock) {
            Schema schema = schemasByName.get(name);
            if (schema != null) {
                throw new IllegalArgumentException(String.format("Schema name %s taken under connector %s", name, schema.getConnector()));
            }
            Connector existingConnector = connectorsByName.get(connector.getName());
            if (existingConnector == null) {
                addConnector(connector);
            }
            else if (existingConnector != connector) {
                throw new IllegalArgumentException("Connector name taken: " + connector.getName());
            }
            schema = new Schema(this, name, connector);
            schemasByName = ImmutableMap.<String, Schema>builder().putAll(schemasByName).put(name, schema).build();
            return schema;
        }
    }

    public <T extends Executor> T addExecutor(T executor)
    {
        synchronized (lock) {
            if (executorsByName.get(executor.getName()) != null) {
                throw new IllegalArgumentException("Executor name taken: " + executor.getName());
            }
            executorsByName = ImmutableMap.<String, Executor>builder().putAll(executorsByName).put(executor.getName(), executor).build();
            return executor;
        }
    }

    public Optional<Schema> getSchemaOptional(String name)
    {
        Schema schema = schemasByName.get(name);
        if (schema != null) {
            return Optional.of(schema);
        }
        for (Catalog parent : parents) {
            Optional<Schema> optional = parent.getSchemaOptional(name);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    public Schema getSchema(String name)
    {
        return getSchemaOptional(name).orElseGet(() -> { throw new IllegalArgumentException("Schema not found: " + name); });
    }

    public Table getSchemaTable(SchemaTable schemaTable)
    {
        return getSchema(schemaTable.getSchema()).getTable(schemaTable.getTable());
    }

    public Function addFunction(String name, Executor executor)
    {
        synchronized (lock) {
            Function function = functionsByName.get(name);
            if (function != null) {
                throw new IllegalArgumentException(String.format("Function name %s taken under executor %s", name, function.getExecutor()));
            }
            Executor existingExecutor = executorsByName.get(executor.getName());
            if (existingExecutor == null) {
                addExecutor(executor);
            }
            else if (existingExecutor != executor) {
                throw new IllegalArgumentException("Executor name taken: " + executor.getName());
            }
            Signature signature = checkNotNull(executor.getExecutable(name)).getSignature();
            function = new Function(this, name, signature, executor);
            functionsByName = ImmutableMap.<String, Function>builder().putAll(functionsByName).put(name, function).build();
            return function;
        }
    }

    public Optional<Function> getFunctionOptional(String name)
    {
        Function function = functionsByName.get(name);
        if (function != null) {
            return Optional.of(function);
        }
        for (Catalog parent : parents) {
            Optional<Function> optional = parent.getFunctionOptional(name);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    public Function getFunction(String name)
    {
        return getFunctionOptional(name).orElseGet(() -> { throw new IllegalArgumentException("Function not found: " + name); });
    }
}
