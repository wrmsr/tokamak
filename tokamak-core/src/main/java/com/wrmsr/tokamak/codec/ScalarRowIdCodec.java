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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.codec.Codec;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ScalarRowIdCodec<V>
        implements RowIdCodec
{
    private final String field;
    private final Codec<V, byte[]> child;

    public ScalarRowIdCodec(String field, Codec<V, byte[]> child)
    {
        this.field = checkNotNull(field);
        this.child = checkNotNull(child);
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
