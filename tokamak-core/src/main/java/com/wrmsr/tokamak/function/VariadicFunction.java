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

public interface VariadicFunction<T>
        extends ScalarFunction<T>
{
    static <T> VariadicFunction<T> of(
            String name,
            List<Type> argTypes,
            Type type,
            java.util.function.Function<Object[], T> fn)
    {
        List<Type> argTypes_ = ImmutableList.copyOf(argTypes);
        return new VariadicFunction<T>()
        {
            @Override
            public String toString()
            {
                return "VariadicFunction{name='" + getName() + "'}";
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
            public List<Type> getArgTypes()
            {
                return argTypes_;
            }

            @Override
            public T invoke(Object... args)
            {
                return fn.apply(args);
            }
        };
    }

    static <T> VariadicFunction<T> anon(
            List<Type> argTypes,
            Type type,
            java.util.function.Function<Object[], T> fn)
    {
        return of(Function.genAnonName(), argTypes, type, fn);
    }
}
