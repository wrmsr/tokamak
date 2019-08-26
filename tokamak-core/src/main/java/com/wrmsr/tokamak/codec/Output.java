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

import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;

public interface Output
{
    int tell();

    void alloc(int sz);

    void put(byte value);

    void putLong(long value);

    void putBytes(byte[] value);

    void putBytes(byte[] value, int offset, int length);

    void putAt(int pos, byte value);

    void putLongAt(int pos, long value);

    void putBytesAt(int pos, byte[] value);

    default OutputStream toOutputStream()
    {
        return new OutputStream()
        {
            @Override
            public void write(int b)
                    throws IOException
            {
                checkArgument((b & 0xFF00) == 0);
                put((byte) b);
            }

            @Override
            public void write(byte[] b)
                    throws IOException
            {
                putBytes(b);
            }

            @Override
            public void write(byte[] b, int off, int len)
                    throws IOException
            {
                putBytes(b, off, len);
            }
        };
    }
}
