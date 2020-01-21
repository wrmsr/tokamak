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
package com.wrmsr.tokamak.core.type;

import com.google.common.primitives.Primitives;
import com.wrmsr.tokamak.core.type.hier.PrimitiveType;
import com.wrmsr.tokamak.core.type.hier.SimpleType;
import com.wrmsr.tokamak.core.type.hier.SpecialType;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeLike;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class TypeRegistration
        implements TypeConstructor
{
    private final String name;
    private final Class<? extends TypeLike> cls;
    private final Optional<java.lang.reflect.Type> reflect;
    private final TypeConstructor constructor;

    public TypeRegistration(
            String name,
            Class<? extends TypeLike> cls,
            Optional<java.lang.reflect.Type> reflect,
            TypeConstructor constructor)
    {
        this.name = checkNotEmpty(name);
        this.cls = checkNotNull(cls);
        this.reflect = checkNotNull(reflect);
        reflect.ifPresent(rfl -> checkArgument(!Primitives.allPrimitiveTypes().contains(rfl)));
        this.constructor = checkNotNull(constructor);
    }

    public TypeRegistration(
            String name,
            Class<? extends TypeLike> cls,
            java.lang.reflect.Type reflect,
            TypeConstructor constructor)
    {
        this(name, cls, Optional.of(reflect), constructor);
    }

    public TypeRegistration(
            String name,
            Class<? extends TypeLike> cls,
            TypeConstructor constructor)
    {
        this(name, cls, Optional.empty(), constructor);
    }

    public String getName()
    {
        return name;
    }

    public Class<? extends TypeLike> getCls()
    {
        return cls;
    }

    public Optional<java.lang.reflect.Type> getReflect()
    {
        return reflect;
    }

    public TypeConstructor getConstructor()
    {
        return constructor;
    }

    @Override
    public Type construct(List<Object> args, Map<String, Object> kwargs)
    {
        return constructor.construct(args, kwargs);
    }

    public static TypeRegistration standard(Type type)
    {
        if (type instanceof PrimitiveType || type instanceof SimpleType) {
            return new TypeRegistration(type.getName(), type.getClass(), type.toReflect(), TypeConstructor.of(() -> type));
        }
        else if (type instanceof SpecialType) {
            return new TypeRegistration(type.getName(), type.getClass(), TypeConstructor.of(() -> type));
        }
        else {
            throw new IllegalArgumentException(Objects.toString(type));
        }
    }
}
