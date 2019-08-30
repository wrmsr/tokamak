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
package com.wrmsr.tokamak.codec.value;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.codec.Width;
import com.wrmsr.tokamak.type.Type;

import java.util.Map;
import java.util.OptionalInt;

public final class ValueCodecs
{
    private ValueCodecs()
    {
    }

    public static final FunctionPairValueCodec<Boolean> BOOLEAN_VALUE_CODEC = FunctionPairValueCodec.of(
            (v, o) -> o.put(v ? (byte) 1 : (byte) 0),
            i -> i.get() != 0,
            Width.fixed(1),
            false
    );

    public static final FunctionPairValueCodec<Number> LONG_VALUE_CODEC = FunctionPairValueCodec.of(
            (v, o) -> o.putLong(v.longValue()),
            i -> i.getLong(),
            Width.fixed(8),
            false
    );

    public static final FunctionPairValueCodec<Number> DOUBLE_VALUE_CODEC = FunctionPairValueCodec.of(
            (v, o) -> o.putLong(Double.doubleToLongBits(v.doubleValue())),
            i -> Double.longBitsToDouble(i.getLong()),
            Width.fixed(8),
            false
    );

    public static FunctionPairValueCodec<byte[]> buildBytes(OptionalInt size)
    {
        return FunctionPairValueCodec.of(
                (v, o) -> o.putBytes(v),
                i -> i.getBytes(),
                Width.of(0, size),
                false
        );
    }

    public static FunctionPairValueCodec<byte[]> buildBytes(int size)
    {
        return buildBytes(OptionalInt.of(size));
    }

    public static final FunctionPairValueCodec<byte[]> BYTES_VALUE_CODEC = buildBytes(OptionalInt.empty());

    public static FunctionPairValueCodec<String> buildString(OptionalInt size)
    {
        return FunctionPairValueCodec.of(
                (v, o) -> o.putBytes(v.getBytes(Charsets.UTF_8)),
                i -> new String(i.getBytes(), Charsets.UTF_8),
                Width.of(0, size),
                false
        );
    }

    public static FunctionPairValueCodec<String> buildString(int size)
    {
        return buildString(OptionalInt.of(size));
    }

    public static final FunctionPairValueCodec<String> STRING_VALUE_CODEC = buildString(OptionalInt.empty());

    @SuppressWarnings({"rawtypes"})
    public static final Map<Type, ValueCodec> VALUE_CODECS_BY_TYPE = ImmutableMap.<Type, ValueCodec>builder()
            .put(Type.BOOLEAN, BOOLEAN_VALUE_CODEC)
            .put(Type.LONG, LONG_VALUE_CODEC)
            .put(Type.DOUBLE, DOUBLE_VALUE_CODEC)
            .put(Type.BYTES, BYTES_VALUE_CODEC)
            .put(Type.STRING, STRING_VALUE_CODEC)
            .build();
}
