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
package com.wrmsr.tokamak.codec;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.type.Type;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;

public final class IdCodecs
{
    private IdCodecs()
    {
    }

    public static final ScalarRowIdCodec.FunctionPair<Boolean> BOOLEAN_SCALAR_PAIR = ScalarRowIdCodec.FunctionPair.of(
            (v, o) -> o.put((Boolean) v ? (byte) 1 : (byte) 0),
            i -> i.get() != 0
    );

    public static final ScalarRowIdCodec.FunctionPair<Long> LONG_SCALAR_PAIR = ScalarRowIdCodec.FunctionPair.of(
            (v, o) -> o.put(((Number) v).longValue()),
            i -> i.getLong()
    );

    public static final ScalarRowIdCodec.FunctionPair<Double> DOUBLE_SCALAR_PAIR = ScalarRowIdCodec.FunctionPair.of(
            (v, o) -> o.put(Double.doubleToLongBits(((Number) v).doubleValue())),
            i -> Double.longBitsToDouble(i.getLong())
    );

    public static final ScalarRowIdCodec.FunctionPair<byte[]> BYTES_SCALAR_PAIR = ScalarRowIdCodec.FunctionPair.of(
            (v, o) -> o.put(v),
            i -> i.getBytes()
    );

    public static final ScalarRowIdCodec.FunctionPair<String> STRING_SCALAR_PAIR = ScalarRowIdCodec.FunctionPair.of(
            (v, o) -> o.put(v.getBytes(Charsets.UTF_8)),
            i -> new String(i.getBytes(), Charsets.UTF_8)
    );

    public static final Map<Type, ScalarRowIdCodec.FunctionPair> SCALAR_PAIRS_BY_TYPE = ImmutableMap.<Type, ScalarRowIdCodec.FunctionPair>builder()
            .put(Type.BOOLEAN, BOOLEAN_SCALAR_PAIR)
            .put(Type.LONG, LONG_SCALAR_PAIR)
            .put(Type.DOUBLE, DOUBLE_SCALAR_PAIR)
            .put(Type.BYTES, BYTES_SCALAR_PAIR)
            .put(Type.STRING, STRING_SCALAR_PAIR)
            .build();

    public static RowIdCodec buildRowIdCodec(Map<String, Type> typesByField)
    {
        checkArgument(!typesByField.isEmpty());
        if (typesByField.size() == 1) {
            Map.Entry<String, Type> e = typesByField.entrySet().stream().collect(toSingle());
            return new ScalarRowIdCodec(e.getKey(), IdCodecs.SCALAR_PAIRS_BY_TYPE.get(e.getValue()));
        }
        else {
            List<RowIdCodec> parts = typesByField.entrySet().stream().map(e ->
                    new ScalarRowIdCodec(e.getKey(), IdCodecs.SCALAR_PAIRS_BY_TYPE.get(e.getValue()))
            ).collect(toImmutableList());
            return new CompositeRowIdCodec(parts);
        }
    }
}
