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
package com.wrmsr.tokamak.codec.scalar;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class MapScalarCodec<K, V>
        extends CollectionScalarCodec<Map<K, V>>
{
    private final ScalarCodec<K> keyChild;
    private final ScalarCodec<V> valueChild;

    public MapScalarCodec(ScalarCodec<K> keyChild, ScalarCodec<V> valueChild, int maxLength)
    {
        super(maxLength);
        this.keyChild = checkNotNull(keyChild);
        this.valueChild = checkNotNull(valueChild);
    }

    public MapScalarCodec(ScalarCodec<K> keyChild, ScalarCodec<V> valueChild)
    {
        this(keyChild, valueChild, DEFAULT_MAX_LENGTH);
    }

    @Override
    public void encode(Map<K, V> value, Output output)
    {
        encodeSize(value.size(), output);
        for (Map.Entry<K, V> entry : value.entrySet()) {
            keyChild.encode(entry.getKey(), output);
            valueChild.encode(entry.getValue(), output);
        }
    }

    @Override
    public Map<K, V> decode(Input input)
    {
        int sz = decodeSize(input);
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.put(keyChild.decode(input), valueChild.decode(input));
        }
        return builder.build();
    }
}
