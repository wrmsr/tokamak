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
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class Catalog
{
    private final List<Catalog> parents;

    private static final class State
    {
        private final Map<String, Connector> connectorsByName;
        private final Map<String, Schema> schemasByName;
        private final Map<String, Executor> executorsByName;
        private final Map<String, Function> functionsByName;
        private final Map<String, View> viewsByName;

        private State(Builder builder)
        {
            connectorsByName = ImmutableMap.copyOf(builder.connectorsByName);
            schemasByName = ImmutableMap.copyOf(builder.schemasByName);
            executorsByName = ImmutableMap.copyOf(builder.executorsByName);
            functionsByName = ImmutableMap.copyOf(builder.functionsByName);
            viewsByName = ImmutableMap.copyOf(builder.viewsByName);
        }

        public static final class Builder
        {
            private Map<String, Connector> connectorsByName = ImmutableMap.of();
            private Map<String, Schema> schemasByName = ImmutableMap.of();
            private Map<String, Executor> executorsByName = ImmutableMap.of();
            private Map<String, Function> functionsByName = ImmutableMap.of();
            private Map<String, View> viewsByName = ImmutableMap.of();

            public Builder()
            {
            }

            public Builder(State state)
            {
                connectorsByName = state.connectorsByName;
                schemasByName = state.schemasByName;
                executorsByName = state.executorsByName;
                functionsByName = state.functionsByName;
                viewsByName = state.viewsByName;
            }

            public Builder connectorsByName(Map<String, Connector> val)
            {
                connectorsByName = val;
                return this;
            }

            public Builder schemasByName(Map<String, Schema> val)
            {
                schemasByName = val;
                return this;
            }

            public Builder executorsByName(Map<String, Executor> val)
            {
                executorsByName = val;
                return this;
            }

            public Builder functionsByName(Map<String, Function> val)
            {
                functionsByName = val;
                return this;
            }

            public Builder viewsByName(Map<String, View> val)
            {
                viewsByName = val;
                return this;
            }

            public State build()
            {
                return new State(this);
            }
        }

        public Builder rebuild()
        {
            return new Builder(this);
        }
    }

    private final Object lock = new Object();
    private volatile State state = new State.Builder().build();

    public Catalog(List<Catalog> parents)
    {
        this.parents = ImmutableList.copyOf(parents);
    }

    public Catalog()
    {
        this(ImmutableList.of());
    }

    @JsonCreator
    private Catalog(
            @JsonProperty("parents") List<Catalog> parents,
            @JsonProperty("connectors") List<Connector> connectors,
            @JsonProperty("schemas") List<Schema> schemas,
            @JsonProperty("executors") List<Executor> executors,
            @JsonProperty("functions") List<Function> functions,
            @JsonProperty("views") List<View> views)
    {
        this.parents = ImmutableList.copyOf(parents);

        checkNotNull(connectors).forEach(this::addConnector);
        Map<String, Schema> schemasByName = new LinkedHashMap<>();
        schemas.forEach(s -> {
            checkState(connectors.contains(s.getConnector()));
            checkState(!schemasByName.containsKey(s.getName()));
            s.setCatalog(this);
            schemasByName.put(s.getName(), s);
        });
        state = state.rebuild().schemasByName(
                ImmutableMap.<String, Schema>builder().putAll(state.schemasByName).putAll(schemasByName).build()
        ).build();

        checkNotNull(executors).forEach(this::addExecutor);
        Map<String, Function> functionsByName = new LinkedHashMap<>();
        functions.forEach(f -> {
            checkState(executors.contains(f.getExecutor()));
            checkState(!schemasByName.containsKey(f.getName()));
            f.setCatalog(this);
            functionsByName.put(f.getName(), f);
        });
        state = state.rebuild().functionsByName(
                ImmutableMap.<String, Function>builder().putAll(state.functionsByName).putAll(functionsByName).build()
        ).build();

        views.forEach(this::addView);
    }

    @JsonProperty("parents")
    public List<Catalog> getParents()
    {
        return parents;
    }

    @JsonProperty("connectors")
    public Collection<Connector> getConnectors()
    {
        return state.connectorsByName.values();
    }

    public Map<String, Connector> getConnectorsByName()
    {
        return state.connectorsByName;
    }

    @JsonProperty("schemas")
    public Collection<Schema> getSchemas()
    {
        return state.schemasByName.values();
    }

    public Map<String, Schema> getSchemasByName()
    {
        return state.schemasByName;
    }

    @JsonProperty("executors")
    public Collection<Executor> getExecutors()
    {
        return state.executorsByName.values();
    }

    public Map<String, Executor> getExecutorsByName()
    {
        return state.executorsByName;
    }

    @JsonProperty("functions")
    public Collection<Function> getFunctions()
    {
        return state.functionsByName.values();
    }

    public Map<String, Function> getFunctionsByName()
    {
        return state.functionsByName;
    }

    @JsonProperty("views")
    public Collection<View> getViews()
    {
        return state.viewsByName.values();
    }

    public Map<String, View> getViewsByName()
    {
        return state.viewsByName;
    }

    public <T extends Connector> T addConnector(T connector)
    {
        synchronized (lock) {
            if (state.connectorsByName.get(connector.getName()) != null) {
                throw new IllegalArgumentException("Connector name taken: " + connector.getName());
            }
            state = state.rebuild().connectorsByName(
                    ImmutableMap.<String, Connector>builder().putAll(state.connectorsByName).put(connector.getName(), connector).build()
            ).build();
            return connector;
        }
    }

    public Schema addSchema(String name, Connector connector)
    {
        synchronized (lock) {
            Schema schema = state.schemasByName.get(name);
            if (schema != null) {
                throw new IllegalArgumentException(String.format("Schema name %s taken under connector %s", name, schema.getConnector()));
            }
            Connector existingConnector = state.connectorsByName.get(connector.getName());
            if (existingConnector == null) {
                addConnector(connector);
            }
            else if (existingConnector != connector) {
                throw new IllegalArgumentException("Connector name taken: " + connector.getName());
            }
            schema = new Schema(this, name, connector);
            state = state.rebuild().schemasByName(
                    ImmutableMap.<String, Schema>builder().putAll(state.schemasByName).put(name, schema).build()
            ).build();
            return schema;
        }
    }

    public <T extends Executor> T addExecutor(T executor)
    {
        synchronized (lock) {
            if (state.executorsByName.get(executor.getName()) != null) {
                throw new IllegalArgumentException("Executor name taken: " + executor.getName());
            }
            state = state.rebuild().executorsByName(
                    ImmutableMap.<String, Executor>builder().putAll(state.executorsByName).put(executor.getName(), executor).build()
            ).build();
            return executor;
        }
    }

    public Optional<Schema> getSchemaOptional(String name)
    {
        Schema schema = state.schemasByName.get(name);
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
            Function function = state.functionsByName.get(name);
            if (function != null) {
                throw new IllegalArgumentException(String.format("Function name %s taken under executor %s", name, function.getExecutor()));
            }
            Executor existingExecutor = state.executorsByName.get(executor.getName());
            if (existingExecutor == null) {
                addExecutor(executor);
            }
            else if (existingExecutor != executor) {
                throw new IllegalArgumentException("Executor name taken: " + executor.getName());
            }
            Executable executable = checkNotNull(executor.getExecutable(name));
            function = new Function(
                    this,
                    name,
                    executable.getType(),
                    executable.getPurity(),
                    executor);
            state = state.rebuild().functionsByName(
                    ImmutableMap.<String, Function>builder().putAll(state.functionsByName).put(name, function).build()
            ).build();
            return function;
        }
    }

    public Optional<Function> getFunctionOptional(String name)
    {
        Function function = state.functionsByName.get(name);
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

    public View addView(View view)
    {
        synchronized (lock) {
            View existingView = state.viewsByName.get(view.getName());
            if (existingView != null) {
                throw new IllegalArgumentException(String.format("Schema name %s taken under connector %s", view.getName(), existingView));
            }
            state = state.rebuild().viewsByName(
                    ImmutableMap.<String, View>builder().putAll(state.viewsByName).put(view.getName(), view).build()
            ).build();
            return view;
        }
    }

    public Optional<View> getViewOptional(String name)
    {
        View view = state.viewsByName.get(name);
        if (view != null) {
            return Optional.of(view);
        }
        for (Catalog parent : parents) {
            Optional<View> optional = parent.getViewOptional(name);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    public View getView(String name)
    {
        return getViewOptional(name).orElseGet(() -> { throw new IllegalArgumentException("View not found: " + name); });
    }
}
