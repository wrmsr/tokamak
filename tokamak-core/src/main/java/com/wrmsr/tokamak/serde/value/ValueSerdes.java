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
package com.wrmsr.tokamak.serde.value;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.serde.Width;
import com.wrmsr.tokamak.type.Type;

import java.util.Map;
import java.util.OptionalInt;

public final class ValueSerdes
{
    private ValueSerdes()
    {
    }

    public static final FunctionPairValueSerde<Boolean> BOOLEAN_VALUE_SERDE = FunctionPairValueSerde.of(
            (v, o) -> o.put(v ? (byte) 1 : (byte) 0),
            i -> i.get() != 0,
            Width.fixed(1),
            false
    );

    public static final FunctionPairValueSerde<Number> LONG_VALUE_SERDE = FunctionPairValueSerde.of(
            (v, o) -> o.putLong(v.longValue()),
            i -> i.getLong(),
            Width.fixed(8),
            false
    );

    public static final FunctionPairValueSerde<Number> DOUBLE_VALUE_SERDE = FunctionPairValueSerde.of(
            (v, o) -> o.putLong(Double.doubleToLongBits(v.doubleValue())),
            i -> Double.longBitsToDouble(i.getLong()),
            Width.fixed(8),
            false
    );

    public static FunctionPairValueSerde<byte[]> buildBytes(OptionalInt size)
    {
        return FunctionPairValueSerde.of(
                (v, o) -> o.putBytes(v),
                i -> i.getBytes(),
                Width.of(0, size),
                false
        );
    }

    public static FunctionPairValueSerde<byte[]> buildBytes(int size)
    {
        return buildBytes(OptionalInt.of(size));
    }

    public static final FunctionPairValueSerde<byte[]> BYTES_VALUE_SERDE = buildBytes(OptionalInt.empty());

    public static FunctionPairValueSerde<String> buildString(OptionalInt size)
    {
        return FunctionPairValueSerde.of(
                (v, o) -> o.putBytes(v.getBytes(Charsets.UTF_8)),
                i -> new String(i.getBytes(), Charsets.UTF_8),
                Width.of(0, size),
                false
        );
    }

    public static FunctionPairValueSerde<String> buildString(int size)
    {
        return buildString(OptionalInt.of(size));
    }

    public static final FunctionPairValueSerde<String> STRING_VALUE_SERDE = buildString(OptionalInt.empty());

    @SuppressWarnings({"rawtypes"})
    public static final Map<Type, ValueSerde> VALUE_SERDES_BY_TYPE = ImmutableMap.<Type, ValueSerde>builder()
            .put(Type.BOOLEAN, BOOLEAN_VALUE_SERDE)
            .put(Type.LONG, LONG_VALUE_SERDE)
            .put(Type.DOUBLE, DOUBLE_VALUE_SERDE)
            .put(Type.BYTES, BYTES_VALUE_SERDE)
            .put(Type.STRING, STRING_VALUE_SERDE)
            .build();
}
