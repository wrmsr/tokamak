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

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkState;

public final class ByteArrayInput
        implements Input
{
    /*
    TODO:
     - check all input consumed
    */

    private final byte[] buf;
    private int pos;
    private int max;

    public ByteArrayInput(byte[] buf)
    {
        this.buf = buf;
        pos = 0;
        max = buf.length;
    }

    public ByteArrayInput(byte[] buf, int offset, int length)
    {
        this.buf = buf;
        this.pos = offset;
        this.max = offset + length;
    }

    @Override
    public byte get()
    {
        checkState(pos < max);
        return buf[pos++];
    }

    @Override
    public long getLong()
    {
        int npos = pos + 8;
        checkState(npos <= max);
        long ret = ByteBuffer.wrap(buf, pos, 8).getLong();
        pos = npos;
        return ret;
    }

    @Override
    public byte[] getBytes()
    {
        checkState(pos < max);
        int sz = max - pos;
        byte[] ret = new byte[sz];
        System.arraycopy(buf, pos, ret, 0, sz);
        pos = max;
        return ret;
    }

    @Override
    public Input nest(int sz)
    {
        checkState(pos + sz <= max);
        Input ret = new ByteArrayInput(buf, pos, sz);
        pos += sz;
        return ret;
    }
}
