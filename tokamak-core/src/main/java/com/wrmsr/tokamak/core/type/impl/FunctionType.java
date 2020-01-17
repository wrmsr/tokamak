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
package com.wrmsr.tokamak.core.type.impl;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;

import javax.annotation.concurrent.Immutable;

import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;

@Immutable
public final class FunctionType
        extends AbstractType
{
    public static final String NAME = "Function";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, FunctionType.class, Method.class, TypeConstructor.of(
            (List<Object> args) -> new FunctionType((Type) args.get(0), immutableMapItems(args.subList(1, args.size()), Type.class::cast))));

    public FunctionType(Type returnType, List<Type> paramTypes)
    {
        super(NAME, ImmutableList.builder().add(returnType).addAll(paramTypes).build());
    }

    public Type getReturn()
    {
        return (Type) checkNotNull(getArgs().get(0));
    }

    public List<Type> getParams()
    {
        return immutableMapItems(getArgs().subList(1, getArgs().size()), Type.class::cast);
    }

    @Override
    public java.lang.reflect.Type toReflect()
    {
        return Method.class;
    }
}
