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
package com.wrmsr.tokamak.core.serde;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.serde.impl.FunctionPairSerde;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.type.hier.Type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.OptionalInt;

public final class Serdes
{
    private Serdes()
    {
    }

    public static final FunctionPairSerde<Boolean> BOOLEAN_VALUE_SERDE = FunctionPairSerde.of(
            (v, o) -> o.put(v ? (byte) 1 : (byte) 0),
            i -> i.get() != 0,
            Width.fixed(1),
            false
    );

    public static final FunctionPairSerde<Number> LONG_VALUE_SERDE = FunctionPairSerde.of(
            (v, o) -> o.putLong(v.longValue()),
            i -> i.getLong(),
            Width.fixed(8),
            false
    );

    public static final FunctionPairSerde<Number> DOUBLE_VALUE_SERDE = FunctionPairSerde.of(
            (v, o) -> o.putLong(Double.doubleToLongBits(v.doubleValue())),
            i -> Double.longBitsToDouble(i.getLong()),
            Width.fixed(8),
            false
    );

    public static FunctionPairSerde<byte[]> buildBytes(OptionalInt size)
    {
        return FunctionPairSerde.of(
                (v, o) -> o.putBytes(v),
                i -> i.getBytes(),
                Width.of(0, size),
                false
        );
    }

    public static FunctionPairSerde<byte[]> buildBytes(int size)
    {
        return buildBytes(OptionalInt.of(size));
    }

    public static final FunctionPairSerde<byte[]> BYTES_VALUE_SERDE = buildBytes(OptionalInt.empty());

    public static FunctionPairSerde<String> buildString(OptionalInt size)
    {
        return FunctionPairSerde.of(
                (v, o) -> o.putBytes(v.getBytes(Charsets.UTF_8)),
                i -> new String(i.getBytes(), Charsets.UTF_8),
                Width.of(0, size),
                false
        );
    }

    public static FunctionPairSerde<String> buildString(int size)
    {
        return buildString(OptionalInt.of(size));
    }

    public static final FunctionPairSerde<String> STRING_VALUE_SERDE = buildString(OptionalInt.empty());

    public static final FunctionPairSerde<BigInteger> BIG_INTEGER_VALUE_SERDE = FunctionPairSerde.of(
            (v, o) -> o.putBytes(v.toString().getBytes(Charsets.UTF_8)),
            i -> new BigInteger(new String(i.getBytes(), Charsets.UTF_8)),
            Width.of(0, OptionalInt.empty()),
            false
    );

    public static final FunctionPairSerde<BigDecimal> BIG_DECIMAL_VALUE_SERDE = FunctionPairSerde.of(
            (v, o) -> o.putBytes(v.toString().getBytes(Charsets.UTF_8)),
            i -> new BigDecimal(new String(i.getBytes(), Charsets.UTF_8)),
            Width.of(0, OptionalInt.empty()),
            false
    );

    @SuppressWarnings({"rawtypes"})
    public static final Map<Type, Serde> VALUE_SERDES_BY_TYPE = ImmutableMap.<Type, Serde>builder()
            .put(Types.Boolean(), BOOLEAN_VALUE_SERDE)
            .put(Types.Long(), LONG_VALUE_SERDE)
            .put(Types.Double(), DOUBLE_VALUE_SERDE)
            .put(Types.Bytes(), BYTES_VALUE_SERDE)
            .put(Types.String(), STRING_VALUE_SERDE)
            .put(Types.BigInteger(), BIG_INTEGER_VALUE_SERDE)
            .put(Types.BigDecimal(), BIG_DECIMAL_VALUE_SERDE)
            .build();
}
