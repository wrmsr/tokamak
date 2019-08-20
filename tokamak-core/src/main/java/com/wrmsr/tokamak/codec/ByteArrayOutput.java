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

import com.wrmsr.tokamak.util.OpenByteArrayOutputStream;

import javax.annotation.concurrent.Immutable;

import java.nio.ByteBuffer;

@Immutable
public final class ByteArrayOutput
        implements Output
{
    private final OpenByteArrayOutputStream bas = new OpenByteArrayOutputStream();

    public byte[] getBuf()
    {
        return bas.getBuf();
    }

    @Override
    public void put(byte value)
    {
        bas.write(value);
    }

    @Override
    public void putLong(long value)
    {
        ByteBuffer.wrap(bas.getBuf()).putLong(value);
    }

    @Override
    public void putBytes(byte[] value)
    {
        ByteBuffer.wrap(bas.getBuf()).put(value);
    }

    @Override
    public int tell()
    {
        return bas.size();
    }

    @Override
    public void alloc(int sz)
    {
        ByteBuffer.wrap(bas.getBuf()).put(new byte[sz]);
    }

    @Override
    public void putAt(int pos, byte value)
    {
        bas.getBuf()[pos] = value;
    }

    @Override
    public void putLongAt(int pos, long value)
    {
        ByteBuffer.wrap(bas.getBuf(), pos, bas.size() - pos).putLong(value);
    }

    @Override
    public void putBytesAt(int pos, byte[] value)
    {
        ByteBuffer.wrap(bas.getBuf(), pos, bas.size() - pos).put(value);
    }
}
