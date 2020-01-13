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

import com.wrmsr.tokamak.util.config.props.BaseConfigPropertyImpl;
import com.wrmsr.tokamak.util.config.props.BooleanConfigProperty;
import com.wrmsr.tokamak.util.config.props.BooleanConfigPropertyImpl;
import com.wrmsr.tokamak.util.config.props.ConfigProperty;
import com.wrmsr.tokamak.util.config.props.ConfigPropertyImpl;
import com.wrmsr.tokamak.util.config.props.IntConfigProperty;
import com.wrmsr.tokamak.util.config.props.IntConfigPropertyImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MoreStrings.splitCamelCase;

public final class ConfigPropertyMetadata
{
    private final ConfigMetadata parent;
    private final Method method;

    private final Type type;
    private final Optional<ConfigDetail> detailAnnotation;
    private final Optional<ConfigDefault> defaultAnnotation;
    private final String name;
    private final List<String> nameParts;
    private final Optional<String> doc;
    private final Optional<Object> defaultValue;

    ConfigPropertyMetadata(ConfigMetadata parent, Method method)
    {
        this.parent = checkNotNull(parent);
        this.method = checkNotNull(method);

        checkArgument(method.getParameterCount() == 0);
        checkArgument(Modifier.isAbstract(method.getModifiers()));

        if (method.getGenericReturnType() instanceof ParameterizedType) {
            ParameterizedType rt = (ParameterizedType) method.getGenericReturnType();
            checkArgument(ConfigProperty.class.isAssignableFrom((Class) rt.getRawType()));
            checkArgument(rt.getActualTypeArguments().length == 1);
            type = rt.getActualTypeArguments()[0];
        }
        else {
            Class<?> rt = method.getReturnType();
            if (rt == BooleanConfigProperty.class) {
                type = boolean.class;
            }
            else if (rt == IntConfigProperty.class) {
                type = int.class;
            }
            else {
                throw new IllegalArgumentException(method.toString());
            }
        }

        String name = method.getName();
        if (method.isAnnotationPresent(ConfigDetail.class)) {
            ConfigDetail detailAnnotation = method.getAnnotation(ConfigDetail.class);
            this.detailAnnotation = Optional.of(detailAnnotation);
            if (!detailAnnotation.name().isEmpty()) {
                name = detailAnnotation.name();
            }
            if (!detailAnnotation.doc().isEmpty()) {
                doc = Optional.of(detailAnnotation.doc());
            }
            else {
                doc = Optional.empty();
            }
        }
        else {
            detailAnnotation = Optional.empty();
            doc = Optional.empty();
        }

        Optional<Object> defaultValue = null;
        String defaultMethodName = "default" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Method defaultMethod;
        try {
            defaultMethod = parent.getCls().getDeclaredMethod(defaultMethodName);
        }
        catch (ReflectiveOperationException e) {
            defaultMethod = null;
        }
        if (defaultMethod != null) {
            checkArgument(Modifier.isStatic(defaultMethod.getModifiers()));
            try {
                defaultValue = Optional.of(defaultMethod.invoke(null));
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        if (method.isAnnotationPresent(ConfigDefault.class)) {
            if (defaultValue != null) {
                throw new IllegalArgumentException("May not specify both default method and annotation: " + name);
            }
            ConfigDefault defaultAnnotation = method.getAnnotation(ConfigDefault.class);
            this.defaultAnnotation = Optional.of(defaultAnnotation);
            defaultValue = Optional.of(defaultAnnotation.value());
        }
        else {
            this.defaultAnnotation = Optional.empty();
        }
        this.defaultValue = defaultValue == null ? Optional.empty() : defaultValue;

        this.name = checkNotEmpty(name);
        checkArgument(!name.startsWith("_"));
        nameParts = immutableMapItems(splitCamelCase(name), String::toLowerCase);
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

    public Type getType()
    {
        return type;
    }

    public Optional<Object> getDefaultValue()
    {
        return defaultValue;
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends BaseConfigPropertyImpl<?>> getImplCls()
    {
        if (type == boolean.class) {
            return BooleanConfigPropertyImpl.class;
        }
        else if (type == int.class) {
            return IntConfigPropertyImpl.class;
        }
        else {
            return (Class) ConfigPropertyImpl.class;
        }
    }
}
