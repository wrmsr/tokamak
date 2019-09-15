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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MoreStrings.splitCamelCase;

public final class ConfigPropertyMetadata
{
    private final ConfigMetadata parent;
    private final Method method;

    private final Optional<ConfigDetail> detail;
    private final String name;
    private final List<String> nameParts;
    private final Optional<String> doc;

    ConfigPropertyMetadata(ConfigMetadata parent, Method method)
    {
        this.parent = checkNotNull(parent);
        this.method = checkNotNull(method);

        checkArgument(method.getParameterCount() == 0);
        checkArgument(Modifier.isAbstract(method.getModifiers()));

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
        nameParts = splitCamelCase(name).stream().map(String::toLowerCase).collect(toImmutableList());
    }

    public ConfigMetadata getParent()
    {
        return parent;
    }

    public Method getMethod()
    {
        return method;
    }

    public String getName()
    {
        return name;
    }

    public Optional<String> getDoc()
    {
        return doc;
    }

    public Class<?> getType()
    {
        return method.getReturnType();
    }

    public Type getGenericType()
    {
        return method.getGenericReturnType();
    }
}
