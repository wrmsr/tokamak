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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrmsr.tokamak.util.SubtypeRegistry;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings({"rawtypes"})
public final class CatalogRegistry
{
    private final SubtypeRegistry<Connector, ConnectorType> connectorTypeRegistry = new SubtypeRegistry<>(Connector.class, ConnectorType.class);
    private final SubtypeRegistry<Executor, ExecutorType> executorTypeRegistry = new SubtypeRegistry<>(Executor.class, ExecutorType.class);

    public void register(ConnectorType connectorType)
    {
        checkNotNull(connectorType);
        connectorTypeRegistry.put(connectorType);
    }

    public void register(ExecutorType executorType)
    {
        checkNotNull(executorType);
        executorTypeRegistry.put(executorType);
    }

    public ObjectMapper register(ObjectMapper objectMapper)
    {
        connectorTypeRegistry.register(objectMapper);
        executorTypeRegistry.register(objectMapper);
        return objectMapper;
    }

    public void checkRegistered(ObjectMapper objectMapper)
    {
        connectorTypeRegistry.checkRegistered(objectMapper);
        executorTypeRegistry.checkRegistered(objectMapper);
    }
}
