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
package com.wrmsr.tokamak.function;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.type.Type;

import java.util.List;

public interface BinaryFunction<T, U, R>
        extends ScalarFunction
{
    R apply(T arg0, U arg1);

    Type getArg0Type();

    Type getArg1Type();

    @Override
    default List<Type> getArgTypes()
    {
        return ImmutableList.of(getArg0Type(), getArg1Type());
    }

    static <T, U, R> BinaryFunction<T, U, R> of(
            String name,
            Type arg0Type,
            Type arg1Type,
            Type type,
            java.util.function.BiFunction<T, U, R> fn)
    {
        return new BinaryFunction<T, U, R>()
        {
            @Override
            public String toString()
            {
                return getClass().getSimpleName() + "{name='" + getName() + "'}";
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
            public Type getArg0Type()
            {
                return arg0Type;
            }

            @Override
            public Type getArg1Type()
            {
                return arg1Type;
            }

            @Override
            public R apply(T arg0, U arg1)
            {
                return fn.apply(arg0, arg1);
            }
        };
    }

    static <T, U, R> BinaryFunction<T, U, R> anon(
            Type arg0Type,
            Type arg1Type,
            Type type,
            java.util.function.BiFunction<T, U, R> fn)
    {
        return of(Function.genAnonName(), arg0Type, arg1Type, type, fn);
    }
}
