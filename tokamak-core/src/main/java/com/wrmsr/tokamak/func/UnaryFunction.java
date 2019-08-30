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
package com.wrmsr.tokamak.func;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.type.Type;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public interface UnaryFunction<T, R>
        extends ValueFunction<R>
{
    R invoke(T arg);

    Type getArgType();

    @Override
    default R invoke(Object... args)
    {
        checkArgument(args.length == 1);
        return invoke((T) args[0]);
    }

    @Override
    default List<Type> getArgTypes()
    {
        return ImmutableList.of(getArgType());
    }

    static <T, R> UnaryFunction<T, R> of(
            String name,
            Type argType,
            Type type,
            java.util.function.Function<T, R> fn)
    {
        return new UnaryFunction<T, R>()
        {
            @Override
            public String toString()
            {
                return "UnaryFunction{name='" + getName() + "'}";
            }

            @Override
            public String getName()
            {
                return name;
            }

            @Override
            public Type getType()
            {
                return type;
            }

            @Override
            public Type getArgType()
            {
                return argType;
            }

            @Override
            public R invoke(T arg)
            {
                return fn.apply(arg);
            }
        };
    }

    static <T, R> UnaryFunction<T, R> anon(
            Type argType,
            Type type,
            java.util.function.Function<T, R> fn)
    {
        return of(Function.genAnonName(), argType, type, fn);
    }
}
