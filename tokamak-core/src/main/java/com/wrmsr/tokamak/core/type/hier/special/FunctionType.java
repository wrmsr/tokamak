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
package com.wrmsr.tokamak.core.type.hier.special;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.hier.Type;

import javax.annotation.concurrent.Immutable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

@Immutable
public final class FunctionType
        implements SpecialType
{
    public static final String NAME = "Function";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, FunctionType.class, Method.class, TypeConstructor.of(
            (List<Object> args) -> new FunctionType((Type) args.get(0), immutableMapItems(args.subList(1, args.size()), Type.class::cast))));

    private final Type value;
    private final List<Type> params;

    public FunctionType(Type value, Iterable<Type> params)
    {
        this.value = value;
        this.params = ImmutableList.copyOf(params);
    }

    public Type getValue()
    {
        return value;
    }

    public List<Type> getParams()
    {
        return params;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public List<Object> getTypeArgs()
    {
        return ImmutableList.builder().add(value).addAll(params).build();
    }

    @Override
    public Optional<java.lang.reflect.Type> toReflect()
    {
        return Optional.of(Method.class);
    }
}
