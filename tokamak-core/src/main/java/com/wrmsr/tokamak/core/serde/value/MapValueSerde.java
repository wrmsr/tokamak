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
package com.wrmsr.tokamak.core.serde.value;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.serde.Input;
import com.wrmsr.tokamak.core.serde.Output;
import com.wrmsr.tokamak.core.serde.Width;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class MapValueSerde<K, V>
        extends CollectionValueSerde<Map<K, V>>
{
    private final ValueSerde<K> keyChild;
    private final ValueSerde<V> valueChild;

    public MapValueSerde(ValueSerde<K> keyChild, ValueSerde<V> valueChild, int size, boolean fixed)
    {
        super(size, fixed);
        this.keyChild = checkNotNull(keyChild);
        this.valueChild = checkNotNull(valueChild);
    }

    public MapValueSerde(ValueSerde<K> keyChild, ValueSerde<V> valueChild)
    {
        this(keyChild, valueChild, DEFAULT_MAX_SIZE, false);
    }

    @Override
    public Width getEntryWidth()
    {
        return Width.sum(keyChild.getWidth(), valueChild.getWidth());
    }

    @Override
    public void write(Map<K, V> value, Output output)
    {
        encodeSize(value.size(), output);
        for (Map.Entry<K, V> entry : value.entrySet()) {
            keyChild.write(entry.getKey(), output);
            valueChild.write(entry.getValue(), output);
        }
    }

    @Override
    public Map<K, V> read(Input input)
    {
        int sz = decodeSize(input);
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.put(keyChild.read(input), valueChild.read(input));
        }
        return builder.build();
    }
}
