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
package com.wrmsr.tokamak;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.jdbc.metadata.ColumnMetaData;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.codec.Codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public final class IdCodecs
{
    private IdCodecs()
    {
    }

    public interface RowIdCodec
            extends Codec<Map<String, Object>, byte[]>
    {
        // TODO: key awareness, length awareness, packed, salting, vints, ...
    }

    public static final class CompositeRowIdCodec
            implements RowIdCodec, Iterable<RowIdCodec>
    {
        public static final int MAX_ID_LENGTH = 254;

        private final List<RowIdCodec> components;

        public CompositeRowIdCodec(List<RowIdCodec> components)
        {
            this.components = ImmutableList.copyOf(components);
        }

        public List<RowIdCodec> getComponents()
        {
            return components;
        }

        @Override
        public Iterator<RowIdCodec> iterator()
        {
            return components.iterator();
        }

        @Override
        public Map<String, Object> decode(byte[] data)
        {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            for (RowIdCodec child : components) {
                byte[] buf = new byte[1];
                checkState(bais.read(buf, 0, 1) == 1);
                byte[] part = new byte[buf[0]];
                checkState(bais.read(part, 0, part.length) == part.length);
                Map<String, Object> out = child.decode(part);
                builder.putAll(out);
            }
            return builder.build();
        }

        @Override
        public byte[] encode(Map<String, Object> data)
        {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (RowIdCodec child : components) {
                    byte[] part = child.encode(data);
                    checkState(part.length < MAX_ID_LENGTH);
                    baos.write(new byte[] {(byte) part.length});
                    baos.write(part);
                }
                return baos.toByteArray();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class ScalarRowIdCodec<V>
            implements RowIdCodec
    {
        private final String field;
        private final Codec<V, byte[]> child;

        public ScalarRowIdCodec(String field, Codec<V, byte[]> child)
        {
            this.field = field;
            this.child = child;
        }

        public String getField()
        {
            return field;
        }

        public Codec<V, byte[]> getChild()
        {
            return child;
        }

        @Override
        public Map<String, Object> decode(byte[] data)
        {
            return ImmutableMap.of(field, child.decode(data));
        }

        @Override
        public byte[] encode(Map<String, Object> data)
        {
            return child.encode((V) data.get(field));
        }
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
