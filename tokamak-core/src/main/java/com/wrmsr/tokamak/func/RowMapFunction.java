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

import com.wrmsr.tokamak.type.Type;

import java.util.Map;

public interface RowMapFunction<T>
        extends Function
{
    T invoke(Map<String, Object> rowMap);

    static <T> RowMapFunction<T> of(String name, Type type, java.util.function.Function<Map<String, Object>, T> fn)
    {
        return new RowMapFunction<T>()
        {
            @Override
            public String toString()
            {
                return "RowMapFunction{name='" + getName() + "'}";
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
            public T invoke(Map<String, Object> rowMap)
            {
                return fn.apply(rowMap);
            }
        };
    }

    static <T> RowMapFunction<T> anon(Type type, java.util.function.Function<Map<String, Object>, T> fn)
    {
        return of(Function.genAnonName(), type, fn);
    }
}
