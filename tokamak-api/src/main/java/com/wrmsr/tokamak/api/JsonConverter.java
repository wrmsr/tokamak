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
package com.wrmsr.tokamak.api;

import java.util.function.Function;

public interface JsonConverter<T, R>
{
    R toJson(T obj);

    T fromJson(R jsonObj);

    static <T, R> JsonConverter<T, R> of(Function<T, R> to, Function<R, T> from)
    {
        return new JsonConverter<T ,R>()
        {
            @Override
            public R toJson(T obj)
            {
                return to.apply(obj);
            }

            @Override
            public T fromJson(R jsonObj)
            {
                return from.apply(jsonObj);
            }
        };
    }
}
