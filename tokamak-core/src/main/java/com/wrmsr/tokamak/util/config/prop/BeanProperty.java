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

package com.wrmsr.tokamak.util.config.prop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wrmsr.tokamak.util.Json;
import com.wrmsr.tokamak.util.config.Config;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class BeanProperty
        extends Property
{
    private final Method setter;
    private final Optional<Method> getter;

    BeanProperty(String name, Method setter, Optional<Method> getter)
    {
        super(name, buildType(setter));
        this.setter = checkNotNull(setter);
        this.getter = checkNotNull(getter);
    }

    private static TypeReference buildType(Method setter)
    {
        checkArgument(setter.getParameterTypes().length == 1);
        return Json.typeReference(setter.getParameterTypes()[0]);
    }

    public Method getSetter()
    {
        return setter;
    }

    public Optional<Method> getGetter()
    {
        return getter;
    }

    @Override
    public Object get(Config cfg)
    {
        try {
            return getter.get().invoke(cfg);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Config cfg, Object value)
    {
        try {
            setter.invoke(cfg, value);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
