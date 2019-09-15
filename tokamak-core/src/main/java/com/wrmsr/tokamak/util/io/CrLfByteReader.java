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
package com.wrmsr.tokamak.util.io;

import com.google.common.base.Charsets;
import com.wrmsr.tokamak.util.OpenByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class CrLfByteReader
{
    public static final byte CR = '\r';
    public static final byte LF = '\n';

    public static final int PEEK_FAIL = -1;

    private final InputStream input;

    private boolean hasPeekByte;
    private byte peekByte;

    public CrLfByteReader(InputStream input)
    {
        this.input = checkNotNull(input);
    }

    public InputStream getInput()
    {
        return input;
    }

    public byte next()
            throws IOException
    {
        if (hasPeekByte) {
            hasPeekByte = false;
            return peekByte;
        }
        else {
            return (byte) input.read();
        }
    }

    public byte[] next(byte[] buf)
            throws IOException
    {
        int length = buf.length;
        if (hasPeekByte) {
            buf[0] = next();
            int read = input.read(buf, 1, length - 1);
            checkState(read == length - 1);
        }
        else {
            int read = input.read(buf);
            checkState(read == length);
        }
        return buf;
    }

    public int peek()
            throws IOException
    {
        if (hasPeekByte) {
            return peekByte;
        }
        else if (input.available() < 1) {
            hasPeekByte = false;
            return PEEK_FAIL;
        }
        else {
            peekByte = (byte) input.read();
            hasPeekByte = true;
            return peekByte;
        }
    }

    public int available()
    {
        try {
            return input.available() + (hasPeekByte ? 1 : 0);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OpenByteArrayOutputStream nextLineStream()
            throws IOException
    {
        OpenByteArrayOutputStream bos = new OpenByteArrayOutputStream();
        while (true) {
            byte b = next();
            if (b == CR && peek() == LF) {
                next();
                return bos;
            }
            bos.write(b);
        }
    }

    public byte[] nextLine()
            throws IOException
    {
        return nextLineStream().toByteArray();
    }

    public String nextLine(Charset charset)
            throws IOException
    {
        OpenByteArrayOutputStream bos = nextLineStream();
        return new String(bos.getBuf(), 0, bos.size(), charset);
    }

    public String nextLineAscii()
            throws IOException
    {
        return nextLine(Charsets.US_ASCII);
    }

    public String nextLineUtf8()
            throws IOException
    {
        return nextLine(Charsets.UTF_8);
    }

    public void nextBlankLine()
            throws IOException
    {
        checkState(next() == CR);
        checkState(next() == LF);
    }
}
