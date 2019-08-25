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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class MapScalarCodec<K, V>
        implements ScalarCodec<Map<K, V>>
{
    public static final int MAX_BYTE_LENGTH = 255;
    public static final int DEFAULT_MAX_LENGTH = MAX_BYTE_LENGTH;

    private final ScalarCodec<K> keyChild;
    private final ScalarCodec<V> valueChild;
    private final int maxLength;
    private final boolean isByte;

    public MapScalarCodec(ScalarCodec<K> keyChild, ScalarCodec<V> valueChild, int maxLength)
    {
        checkArgument(maxLength > 0);
        this.keyChild = checkNotNull(keyChild);
        this.valueChild = checkNotNull(valueChild);
        this.maxLength = maxLength;
        isByte = maxLength <= MAX_BYTE_LENGTH;
    }

    public MapScalarCodec(ScalarCodec<K> keyChild, ScalarCodec<V> valueChild)
    {
        this(keyChild, valueChild, DEFAULT_MAX_LENGTH);
    }

    @Override
    public void encode(Map<K, V> value, Output output)
    {
        if (isByte) {
            checkArgument(value.size() < DEFAULT_MAX_LENGTH);
            output.put((byte) value.size());
        }
        else {
            output.putLong(value.size());
        }
        for (Map.Entry<K, V> entry : value.entrySet()) {
            keyChild.encode(entry.getKey(), output);
            valueChild.encode(entry.getValue(), output);
        }
    }

    @Override
    public Map<K, V> decode(Input input)
    {
        int sz;
        if (isByte) {
            sz = input.get() & 0xFF;
        }
        else {
            sz = (int) input.getLong();
        }
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (int i = 0; i < sz; ++i) {
            builder.put(keyChild.decode(input), valueChild.decode(input));
        }
        return builder.build();
    }
}
