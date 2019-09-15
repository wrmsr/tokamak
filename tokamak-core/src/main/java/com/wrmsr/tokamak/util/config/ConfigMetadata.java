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
package com.wrmsr.tokamak.util.config;

import com.wrmsr.tokamak.util.config.props.ConfigProperty;
import com.wrmsr.tokamak.util.config.props.ConfigPropertyImpl;
import com.wrmsr.tokamak.util.config.props.IntConfigProperty;
import com.wrmsr.tokamak.util.config.props.IntConfigPropertyImpl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ConfigMetadata
{
    private final Class<? extends Config> cls;

    private final Map<String, ConfigPropertyMetadata> properties;

    public ConfigMetadata(Class<? extends Config> cls)
    {
        this.cls = checkNotNull(cls);

        checkArgument(Config.class.isAssignableFrom(cls));
        Map<String, ConfigPropertyMetadata> properties = new LinkedHashMap<>();
        for (Class<?> cur = cls; (cur != null) && !cur.equals(Object.class); cur = cur.getSuperclass()) {
            for (Method method : cur.getDeclaredMethods()) {
                if (ConfigProperty.class.isAssignableFrom(method.getReturnType())) {
                    ConfigPropertyMetadata prop = new ConfigPropertyMetadata(this, method);
                    properties.put(prop.getName(), prop);
                }
            }
        }
        this.properties = Collections.unmodifiableMap(properties);
    }

    @Override
    public String toString()
    {
        return "ConfigMetadata{" +
                "cls=" + cls +
                '}';
    }

    public Class<? extends Config> getCls()
    {
        return cls;
    }

    public Map<String, ConfigPropertyMetadata> getProperties()
    {
        return properties;
    }

    public String getPropertyImplBuilderMethodName(ConfigPropertyMetadata pmd)
    {
        if (pmd.getType() == int.class) {
            return "buildIntPropertyImpl";
        }
        else {
            return "buildPropertyImpl";
        }
    }

    public <T> ConfigPropertyImpl<T> buildPropertyImpl(String name, Supplier<T> getter, Consumer<T> setter)
    {
        return new ConfigPropertyImpl<>(
                properties.get(name),
                getter,
                setter);
    }

    public IntConfigProperty buildIntPropertyImpl(String name, IntSupplier getter, IntConsumer setter)
    {
        return new IntConfigPropertyImpl(
                properties.get(name),
                getter,
                setter);
    }
}
