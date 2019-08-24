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

import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.type.Type;

public interface RowFunction<T>
        extends Function
{
    T invoke(Row row);

    static <T> RowFunction<T> of(String name, Type type, java.util.function.Function<Row, T> fn)
    {
        return new RowFunction<T>()
        {
            @Override
            public String toString()
            {
                return "RowFunction{name='" + getName() + "'}";
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
            public T invoke(Row row)
            {
                return fn.apply(row);
            }
        };
    }

    static <T> RowFunction<T> anon(Type type, java.util.function.Function<Row, T> fn)
    {
        return of(Function.genAnonName(), type, fn);
    }
}
