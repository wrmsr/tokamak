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

import com.wrmsr.tokamak.util.codec.Codec;

import java.util.function.BiFunction;

public interface Serializer<T>
{
    T decode(SerdesContext ctx, Object data);

    Object encode(SerdesContext ctx, T object);

    static <T> Serializer<T> of(
            BiFunction<SerdesContext, T, Object> encoder,
            BiFunction<SerdesContext, Object, T> decoder)
    {
        return new Serializer<T>() {
            @Override
            public T decode(SerdesContext ctx, Object data)
            {
                return decoder.apply(ctx, data);
            }

            @Override
            public Object encode(SerdesContext ctx, T object)
            {
                return encoder.apply(ctx, object);
            }
        };
    }

    default Codec<T, Object> bind(SerdesContext ctx)
    {
        return new Codec<T, Object>()
        {
            @Override
            public T decode(Object data)
            {
                return Serializer.this.decode(ctx, data);
            }

            @Override
            public Object encode(T data)
            {
                return Serializer.this.encode(ctx, data);
            }
        };
    }
}
