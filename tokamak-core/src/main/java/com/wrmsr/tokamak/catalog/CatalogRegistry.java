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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

@SuppressWarnings({"rawtypes"})
public final class CatalogRegistry
{
    private final Object lock = new Object();

    private final Map<String, ConnectorType> connectorTypesByName = new HashMap<>();
    private final Map<Class<? extends Connector>, ConnectorType> connectorTypesByCls = new HashMap<>();

    private final Map<String, FunctionExecutorType> functionExecutorTypesByName = new HashMap<>();
    private final Map<Class<? extends FunctionExecutor>, FunctionExecutorType> functionExecutorTypesByCls = new HashMap<>();

    public Map<String, ConnectorType> getConnectorTypesByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(connectorTypesByName);
        }
    }

    public Map<Class<? extends Connector>, ConnectorType> getConnectorTypesByCls()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(connectorTypesByCls);
        }
    }

    public ConnectorType getConnectorType(String name)
    {
        return connectorTypesByName.get(name);
    }

    public Map<String, FunctionExecutorType> getFunctionExecutorTypesByName()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(functionExecutorTypesByName);
        }
    }

    public Map<Class<? extends FunctionExecutor>, FunctionExecutorType> getFunctionExecutorTypesByCls()
    {
        synchronized (lock) {
            return ImmutableMap.copyOf(functionExecutorTypesByCls);
        }
    }

    public FunctionExecutorType getFunctionExecutorType(String name)
    {
        return functionExecutorTypesByName.get(name);
    }

    public void register(ConnectorType<?> connectorType)
    {
        checkNotNull(connectorType);
        synchronized (lock) {
            checkArgument(!connectorTypesByName.containsKey(connectorType.getName()));
            checkArgument(!connectorTypesByCls.containsKey(connectorType.getCls()));
            connectorTypesByName.put(connectorType.getName(), connectorType);
            connectorTypesByCls.put(connectorType.getCls(), connectorType);
        }
    }

    public void register(FunctionExecutorType<?> functionExecutorType)
    {
        checkNotNull(functionExecutorType);
        synchronized (lock) {
            checkArgument(!functionExecutorTypesByName.containsKey(functionExecutorType.getName()));
            checkArgument(!functionExecutorTypesByCls.containsKey(functionExecutorType.getCls()));
            functionExecutorTypesByName.put(functionExecutorType.getName(), functionExecutorType);
            functionExecutorTypesByCls.put(functionExecutorType.getCls(), functionExecutorType);
        }
    }

    public ObjectMapper registerSubtypes(ObjectMapper objectMapper)
    {
        for (ConnectorType connectorType : connectorTypesByName.values()) {
            objectMapper.registerSubtypes(new NamedType(connectorType.getCls(), connectorType.getName()));
        }
        for (FunctionExecutorType functionExecutorType : functionExecutorTypesByName.values()) {
            objectMapper.registerSubtypes(new NamedType(functionExecutorType.getCls(), functionExecutorType.getName()));
        }
        return objectMapper;
    }

    public static <T> void checkConnectorSubtypeRegistered(
            ObjectMapper objectMapper,
            Class<T> cls,
            Iterable<Class<? extends T>> subclsList)
    {
        Collection<NamedType> subtypes = objectMapper.getSubtypeResolver().collectAndResolveSubtypesByTypeId(
                objectMapper.getSerializationConfig(),
                objectMapper.getSerializationConfig().introspect(objectMapper.getTypeFactory().constructType(cls)).getClassInfo());
        subtypes.forEach(st -> checkNotNull(st.getName()));
        Set<Class> subtypeSet = subtypes.stream().map(NamedType::getType).collect(toImmutableSet());
        for (Class<? extends T> subcls : subclsList) {
            if (!subtypeSet.contains(subcls)) {
                throw new IllegalStateException("Subtype type not registered: " + subcls);
            }
        }
    }

    public void checkConnectorSubtypeRegistered(ObjectMapper objectMapper)
    {
        checkConnectorSubtypeRegistered(objectMapper, Connector.class, connectorTypesByCls.keySet());
        checkConnectorSubtypeRegistered(objectMapper, FunctionExecutor.class, functionExecutorTypesByCls.keySet());
    }
}
