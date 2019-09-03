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
import com.google.common.primitives.Bytes;
import com.wrmsr.tokamak.util.OpenByteArrayOutputStream;
import com.wrmsr.tokamak.util.box.Box;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkState;

public class RedisTest
        extends TestCase
{
    /*
    https://redis.io/topics/protocol
    */

    public static final Charset CHARSET = Charsets.UTF_8;

    public static final byte CR = '\r';
    public static final byte LF = '\n';
    public static final byte[] CRLF = new byte[] {CR, LF};

    public static final byte PREFIX_SIMPLE_STRING = '+';
    public static final byte PREFIX_ERROR = '-';
    public static final byte PREFIX_INTEGER = ':';
    public static final byte PREFIX_BULK_STRING = '$';
    public static final byte PREFIX_ARRAY = '*';

    public static final byte[] SUFFIX = CRLF;

    public static final byte[] NULL_BULK_STRING = new byte[] {'$', '-', '1', '\r', '\n'};

    public static void encodeResp(OutputStream output, String value)
            throws IOException
    {
        byte[] bytes = value.getBytes(CHARSET);
        checkState(Bytes.indexOf(bytes, CR) < 0 && Bytes.indexOf(bytes, LF) < 0);
        output.write(PREFIX_SIMPLE_STRING);
        output.write(bytes);
        output.write(SUFFIX);
    }

    public static void encodeResp(OutputStream output, byte value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Byte.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeResp(OutputStream output, short value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Short.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeResp(OutputStream output, int value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Integer.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeResp(OutputStream output, long value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Long.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeResp(OutputStream output, byte[] value)
            throws IOException
    {
        output.write(PREFIX_BULK_STRING);
        output.write(Integer.toString(value.length).getBytes(Charsets.US_ASCII));
        output.write(CRLF);
        output.write(value);
        output.write(CRLF);
    }

    public static void encodeResp(OutputStream output, Object value)
            throws IOException
    {
        if (value instanceof Long || value instanceof Integer) {
            output.write(PREFIX_INTEGER);
        }
    }

    public static final class Error
            extends Box<String>
    {
        public Error(String value)
        {
            super(value);
        }
    }

    private static int PEEK_FAIL = -1;

    public static Iterator<Object> decodeResp(InputStream input)
    {
        return new Iterator<Object>()
        {
            private boolean hasPeekByte;
            private byte peekByte;

            private byte nextByte()
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

            private int peek()
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

            @Override
            public boolean hasNext()
            {
                try {
                    return input.available() > 0;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            private OpenByteArrayOutputStream readLineByteArrayOutputStream()
                    throws IOException
            {
                OpenByteArrayOutputStream bos = new OpenByteArrayOutputStream();
                while (true) {
                    byte b = nextByte();
                    if (b == CR && peek() == LF) {
                        return bos;
                    }
                    bos.write(b);
                }
            }

            private byte[] readLineBytes()
                    throws IOException
            {
                return readLineByteArrayOutputStream().toByteArray();
            }

            private String readLineString()
                    throws IOException
            {
                OpenByteArrayOutputStream bos = readLineByteArrayOutputStream();
                return new String(bos.getBuf(), 0, bos.size(), CHARSET);
            }

            @Override
            public Object next()
            {
                try {
                    byte prefix = (byte) input.read();

                    switch (prefix) {
                        case PREFIX_SIMPLE_STRING: {
                            return readLineBytes();
                        }
                        case PREFIX_ERROR: {
                            return new Error(readLineString());
                        }
                        case PREFIX_INTEGER: {
                            return Long.parseLong(readLineString());
                        }
                        case PREFIX_BULK_STRING: {
                            int length = Integer.parseInt(readLineString());
                            byte[] buf = new byte[length];
                            int read = input.read(buf);
                            checkState(read == length);
                            return buf;
                        }
                        case PREFIX_ARRAY: {
                            break;
                        }
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public void testRedis()
            throws Throwable
    {
        // byte[] buf;
        // Bytes.indexOf(buf, SUFFIX)
    }
}
