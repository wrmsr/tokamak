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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MoreStrings.splitCamelCase;

public final class ConfigMetadata
{
    public final class Property
    {
        private final Method method;

        private final Optional<ConfigDetail> detail;
        private final String name;
        private final List<String> nameParts;
        private final Optional<String> doc;

        private Property(Method method)
        {
            this.method = method;

            checkArgument(method.getParameterCount() == 0);

            String name = method.getName();
            if (method.isAnnotationPresent(ConfigDetail.class)) {
                ConfigDetail detail = method.getAnnotation(ConfigDetail.class);
                this.detail = Optional.of(detail);
                if (!detail.name().isEmpty()) {
                    name = detail.name();
                }
                if (!detail.doc().isEmpty()) {
                    doc = Optional.of(detail.doc());
                }
                else {
                    doc = Optional.empty();
                }
            }
            else {
                detail = Optional.empty();
                doc = Optional.empty();
            }

            this.name = checkNotEmpty(name);
            nameParts = splitCamelCase(name);
        }

        public String getName()
        {
            return name;
        }
    }

    private final Class<? extends Config> cls;

    private final Map<String, Property> properties;

    public ConfigMetadata(Class<? extends Config> cls)
    {
        this.cls = checkNotNull(cls);

        Map<String, Property> properties = new LinkedHashMap<>();
        for (Class<?> cur = cls; (cur != null) && !cur.equals(Object.class); cur = cur.getSuperclass()) {
            for (Method method : cur.getDeclaredMethods()) {
                if (ConfigProperty.class.isAssignableFrom(method.getReturnType())) {
                    Property prop = new Property(method);
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

    public Map<String, Property> getProperties()
    {
        return properties;
    }
}
