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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.type.Type;

import java.util.Map;
import java.util.OptionalInt;

public final class ScalarCodecs
{
    private ScalarCodecs()
    {
    }

    public static final FunctionPairScalarCodec<Boolean> BOOLEAN_SCALAR_CODEC = FunctionPairScalarCodec.of(
            (v, o) -> o.put(v ? (byte) 1 : (byte) 0),
            i -> i.get() != 0,
            OptionalInt.of(1),
            false
    );

    public static final FunctionPairScalarCodec<Number> LONG_SCALAR_CODEC = FunctionPairScalarCodec.of(
            (v, o) -> o.putLong(v.longValue()),
            i -> i.getLong(),
            OptionalInt.of(8),
            false
    );

    public static final FunctionPairScalarCodec<Number> DOUBLE_SCALAR_CODEC = FunctionPairScalarCodec.of(
            (v, o) -> o.putLong(Double.doubleToLongBits(v.doubleValue())),
            i -> Double.longBitsToDouble(i.getLong()),
            OptionalInt.of(8),
            false
    );

    public static FunctionPairScalarCodec<byte[]> buildBytes(OptionalInt size)
    {
        return FunctionPairScalarCodec.of(
                (v, o) -> o.putBytes(v),
                i -> i.getBytes(),
                size,
                false
        );
    }

    public static final FunctionPairScalarCodec<byte[]> BYTES_SCALAR_CODEC = buildBytes(OptionalInt.empty());

    public static FunctionPairScalarCodec<String> buildString(OptionalInt size)
    {
        return FunctionPairScalarCodec.of(
                (v, o) -> o.putBytes(v.getBytes(Charsets.UTF_8)),
                i -> new String(i.getBytes(), Charsets.UTF_8),
                size,
                false
        );
    }

    public static final FunctionPairScalarCodec<String> STRING_SCALAR_CODEC = buildString(OptionalInt.empty());

    @SuppressWarnings({"rawtypes"})
    public static final Map<Type, ScalarCodec> SCALAR_CODECS_BY_TYPE = ImmutableMap.<Type, ScalarCodec>builder()
            .put(Type.BOOLEAN, BOOLEAN_SCALAR_CODEC)
            .put(Type.LONG, LONG_SCALAR_CODEC)
            .put(Type.DOUBLE, DOUBLE_SCALAR_CODEC)
            .put(Type.BYTES, BYTES_SCALAR_CODEC)
            .put(Type.STRING, STRING_SCALAR_CODEC)
            .build();
}
