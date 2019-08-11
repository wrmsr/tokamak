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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.StreamableIterable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public final class CompositeRowIdCodec
        implements RowIdCodec, StreamableIterable<RowIdCodec>
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

    public static List<byte[]> split(byte[] data)
    {
        ImmutableList.Builder<byte[]> parts = ImmutableList.builder();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        while (bais.available() > 0) {
            byte[] buf = new byte[1];
            checkState(bais.read(buf, 0, 1) == 1);
            byte[] part = new byte[buf[0]];
            checkState(bais.read(part, 0, part.length) == part.length);
            parts.add(part);
        }
        return parts.build();
    }

    public static byte[] join(Iterable<byte[]> parts)
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte[] part : parts) {
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

    @Override
    public Map<String, Object> decode(byte[] data)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        List<byte[]> parts = split(data);
        checkState(parts.size() == components.size());
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        for (int i = 0; i < components.size(); ++i) {
            Map<String, Object> out = components.get(i).decode(parts.get(i));
            builder.putAll(out);
        }
        return builder.build();
    }

    @Override
    public byte[] encode(Map<String, Object> data)
    {
        ImmutableList.Builder<byte[]> parts = ImmutableList.builder();
        for (int i = 0; i < components.size(); ++i) {
            parts.add(components.get(i).encode(data));
        }
        return join(parts.build());
    }
}
