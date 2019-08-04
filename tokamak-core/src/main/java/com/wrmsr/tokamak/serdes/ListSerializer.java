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
package com.wrmsr.tokamak.serdes;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.BiFunction;

public abstract class ListSerializer<T>
        implements Serializer<T>
{
    @Override
    public T decode(SerdesContext ctx, Object data)
    {
        return decodeList(ctx, (List<Object>) data);
    }

    protected abstract T decodeList(SerdesContext ctx, List<Object> data);

    @Override
    public Object encode(SerdesContext ctx, T object)
    {
        return encodeList(ctx, object);
    }

    protected abstract List<Object> encodeList(SerdesContext ctx, T object);

    public static <T> ListSerializer<T> of(
            BiFunction<SerdesContext, T, List<Object>> encoder,
            BiFunction<SerdesContext, List<Object>, T> decoder)
    {
        return new ListSerializer<T>()
        {
            @Override
            protected T decodeList(SerdesContext ctx, List<Object> data)
            {
                return decoder.apply(ctx, data);
            }

            @Override
            protected List<Object> encodeList(SerdesContext ctx, T object)
            {
                return encoder.apply(ctx, object);
            }
        };
    }

    public static <E> ListSerializer<List<E>> of(Class<? extends E> cls)
    {
        return new ListSerializer<List<E>>()
        {
            @Override
            protected List<E> decodeList(SerdesContext ctx, List<Object> data)
            {
                ImmutableList.Builder<E> builder = ImmutableList.builder();
                for (Object ele : data) {
                    builder.add(ctx.decode(cls, ele));
                }
                return builder.build();
            }

            @Override
            protected List<Object> encodeList(SerdesContext ctx, List<E> object)
            {
                ImmutableList.Builder<Object> builder = ImmutableList.builder();
                for (Object ele : object) {
                    builder.add(ctx.encode(ele));
                }
                return builder.build();
            }
        };
    }
}
