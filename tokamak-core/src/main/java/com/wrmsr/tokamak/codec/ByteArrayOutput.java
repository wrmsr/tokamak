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

import javax.annotation.concurrent.Immutable;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Immutable
public final class ByteArrayOutput
        implements Output
{
    private byte[] buf;
    private int pos;

    public ByteArrayOutput()
    {
        buf = new byte[32];
    }

    public ByteArrayOutput(int sz)
    {
        buf = new byte[nearestPowerOfTwo(sz)];
    }

    private static int nearestPowerOfTwo(int n)
    {
        return 1 << (32 - Integer.numberOfLeadingZeros(n));
    }

    public byte[] getBuf()
    {
        return buf;
    }

    public byte[] toByteArray()
    {
        if (pos != buf.length) {
            return Arrays.copyOf(buf, pos);
        }
        else {
            return buf;
        }
    }

    public ByteBuffer wrap()
    {
        return ByteBuffer.wrap(buf, pos, buf.length - pos);
    }

    @Override
    public int tell()
    {
        return pos;
    }

    @Override
    public void alloc(int sz)
    {
        int nlen = pos + sz;
        if (nlen > buf.length) {
            buf = Arrays.copyOf(buf, nearestPowerOfTwo(nlen));
        }
    }

    @Override
    public void put(byte value)
    {
        alloc(1);
        buf[pos++] = value;
    }

    @Override
    public void putLong(long value)
    {
        alloc(8);
        wrap().putLong(value);
        pos += 8;
    }

    @Override
    public void putBytes(byte[] value)
    {
        alloc(value.length);
        System.arraycopy(value, 0, buf, pos, value.length);
        pos += value.length;
    }

    @Override
    public void putBytes(byte[] value, int offset, int length)
    {
        alloc(length);
        System.arraycopy(value, offset, buf, pos, length);
        pos += length;
    }

    @Override
    public void putAt(int pos, byte value)
    {
        buf[pos] = value;
    }

    @Override
    public void putLongAt(int pos, long value)
    {
        ByteBuffer.wrap(buf, pos, 8).putLong(value);
    }

    @Override
    public void putBytesAt(int pos, byte[] value)
    {
        System.arraycopy(value, 0, buf, pos, value.length);
    }
}
