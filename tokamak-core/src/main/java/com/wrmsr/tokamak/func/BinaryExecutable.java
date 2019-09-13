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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.type.Type;

import java.util.List;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;

public interface BinaryExecutable<T, U, R>
        extends ValueExecutable<R>
{
    R invoke(T arg0, U arg1);

    Type getArg0Type();

    Type getArg1Type();

    @Override
    default R invoke(Object... args)
    {
        checkArgument(args.length == 2);
        return invoke((T) args[0], (U) args[1]);
    }

    @Override
    default List<Type> getArgTypes()
    {
        return ImmutableList.of(getArg0Type(), getArg1Type());
    }

    static <T, U, R> BinaryExecutable<T, U, R> of(
            String name,
            String arg0Name,
            Type arg0Type,
            String arg1Name,
            Type arg1Type,
            Type type,
            BiFunction<T, U, R> fn)
    {
        Signature signature = new Signature(type, ImmutableMap.of(arg0Name, arg0Type, arg1Name, arg1Type));
        return new BinaryExecutable<T, U, R>()
        {
            @Override
            public String toString()
            {
                return "BinaryFunction{name='" + getName() + "'}";
            }

            @Override
            public String getName()
            {
                return name;
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
            public R invoke(T arg0, U arg1)
            {
                return fn.apply(arg0, arg1);
            }
        };
    }

    static <T, U, R> BinaryExecutable<T, U, R> anon(
            Type arg0Type,
            Type arg1Type,
            Type type,
            BiFunction<T, U, R> fn)
    {
        return of(Executable.genAnonName(), arg0Type, arg1Type, type, fn);
    }
}
