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

import java.util.Map;
import java.util.function.BiFunction;

public abstract class MapSerializer<T>
        implements Serializer<T>
{
    @Override
    public T decode(SerdesContext ctx, Object data)
    {
        return decodeMap(ctx, (Map<String, Object>) data);
    }

    protected abstract T decodeMap(SerdesContext ctx, Map<String, Object> data);

    @Override
    public Object encode(SerdesContext ctx, T object)
    {
        return encodeMap(ctx, object);
    }

    protected abstract Map<String, Object> encodeMap(SerdesContext ctx, T object);

    static <T> MapSerializer<T> of(
            BiFunction<SerdesContext, T, Map<String, Object>> encoder,
            BiFunction<SerdesContext, Map<String, Object>, T> decoder)
    {
        return new MapSerializer<T>()
        {
            @Override
            protected T decodeMap(SerdesContext ctx, Map<String, Object> data)
            {
                return decoder.apply(ctx, data);
            }

            @Override
            protected Map<String, Object> encodeMap(SerdesContext ctx, T object)
            {
                return encoder.apply(ctx, object);
            }
        };
    }
}
