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
import com.wrmsr.tokamak.util.codec.Codec;

import java.nio.ByteBuffer;
import java.util.Map;

public final class IdCodecs
{
    private IdCodecs()
    {
    }

    private static final byte[] BYTES_ZERO = new byte[] {(byte) 0};
    private static final byte[] BYTES_ONE = new byte[] {(byte) 1};

    public static final Codec<Object, byte[]> BOOLEAN_CODEC = Codec.of(
            v -> ((Boolean) v) ? BYTES_ONE : BYTES_ZERO,
            b -> ByteBuffer.wrap(b).get() != 0
    );

    public static final Codec<Object, byte[]> LONG_CODEC = Codec.of(
            v -> ByteBuffer.allocate(8).putLong(((Number) v).longValue()).array(),
            b -> ByteBuffer.wrap(b).getLong()
    );

    public static final Codec<Object, byte[]> DOUBLE_CODEC = Codec.of(
            v -> ByteBuffer.allocate(8).putDouble((double) v).array(),
            b -> ByteBuffer.wrap(b).getLong()
    );

    public static final Codec<Object, byte[]> STRING_CODEC = Codec.of(
            v -> ((String) v).getBytes(Charsets.UTF_8),
            b -> new String(b, 0, b.length, Charsets.UTF_8)
    );

    public static final Codec<Object, byte[]> BYTES_CODEC = Codec.of(
            v -> (byte[]) v,
            b -> b
    );

    public static final Map<Type, Codec<Object, byte[]>> CODECS_BY_TYPE;

    static {
        ImmutableMap.Builder<Type, Codec<Object, byte[]>> builder = ImmutableMap.builder();

        builder.put(Type.BOOLEAN, BOOLEAN_CODEC);
        builder.put(Type.LONG, LONG_CODEC);
        builder.put(Type.DOUBLE, DOUBLE_CODEC);
        builder.put(Type.STRING, STRING_CODEC);
        builder.put(Type.BYTES, BYTES_CODEC);

        CODECS_BY_TYPE = builder.build();
    }
}
