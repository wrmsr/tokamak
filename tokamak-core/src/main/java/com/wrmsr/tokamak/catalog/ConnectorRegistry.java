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
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class ConnectorRegistry
{
    private final Object lock = new Object();

    private final Map<String, ConnectorType> connectorTypesByName = new HashMap<>();
    private final Map<Class<? extends Connector>, ConnectorType> connectorTypesByCls = new HashMap<>();

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

    public ConnectorType get(String name)
    {
        return connectorTypesByName.get(name);
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

    public ObjectMapper registerSubtypes(ObjectMapper objectMapper)
    {
        for (ConnectorType connectorType : connectorTypesByName.values()) {
            objectMapper.registerSubtypes(new NamedType(connectorType.getCls(), connectorType.getName()));
        }
        return objectMapper;
    }

    public static void checkConnectorSubtypeRegistered(ObjectMapper objectMapper, Iterable<Class<? extends Connector>> clsList)
    {
        Collection<NamedType> subtypes = objectMapper.getSubtypeResolver().collectAndResolveSubtypesByTypeId(
                objectMapper.getSerializationConfig(),
                objectMapper.getSerializationConfig().introspect(SimpleType.construct(Connector.class)).getClassInfo());
        subtypes.forEach(st -> checkNotNull(st.getName()));
        Set<Class> subtypeSet = subtypes.stream().map(NamedType::getType).collect(toImmutableSet());
        for (Class<? extends Connector> cls : clsList) {
            if (!subtypeSet.contains(cls)) {
                throw new IllegalStateException("Connector type not registered: " + cls);
            }
        }
    }

    public void checkConnectorSubtypeRegistered(ObjectMapper objectMapper)
    {
        checkConnectorSubtypeRegistered(objectMapper, connectorTypesByCls.keySet());
    }
}
