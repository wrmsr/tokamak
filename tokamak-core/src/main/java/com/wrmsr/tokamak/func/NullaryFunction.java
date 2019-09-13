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
import java.util.function.Supplier;

public interface NullaryFunction<T>
        extends ValueFunction<T>
{
    T invoke();

    @Override
    default T invoke(Object... args)
    {
        return invoke();
    }

    @Override
    default List<Type> getArgTypes()
    {
        return ImmutableList.of();
    }

    static <T> NullaryFunction<T> of(
            String name,
            Type type,
            Supplier<T> fn)
    {
        return new NullaryFunction<T>()
        {
            @Override
            public String toString()
            {
                return "NullaryFunction{name='" + getName() + "'}";
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
            public T invoke()
            {
                return fn.get();
            }
        };
    }

    static <T> NullaryFunction<T> anon(
            Type type,
            Supplier<T> fn)
    {
        return of(Function.genAnonName(), type, fn);
    }
}
