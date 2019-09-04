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
import com.google.common.primitives.Bytes;
import com.wrmsr.tokamak.util.OpenByteArrayOutputStream;
import com.wrmsr.tokamak.util.box.Box;
import junit.framework.TestCase;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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

    public static void encodeRespNull(OutputStream output)
            throws IOException
    {
        output.write(NULL_BULK_STRING);
    }

    public static void encodeRespByte(OutputStream output, byte value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Byte.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeRespShort(OutputStream output, short value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Short.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeRespInt(OutputStream output, int value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Integer.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeRespLong(OutputStream output, long value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Long.toString(value).getBytes(Charsets.US_ASCII));
        output.write(SUFFIX);
    }

    public static void encodeRespBytes(OutputStream output, byte[] value)
            throws IOException
    {
        output.write(PREFIX_BULK_STRING);
        if (Bytes.indexOf(value, CR) < 0 && Bytes.indexOf(value, LF) < 0) {
            output.write(PREFIX_SIMPLE_STRING);
            output.write(value);
        }
        else {
            output.write(Integer.toString(value.length).getBytes(Charsets.US_ASCII));
            output.write(CRLF);
            output.write(value);
        }
        output.write(SUFFIX);
    }

    public static void encodeRespString(OutputStream output, String value)
            throws IOException
    {
        encodeRespBytes(output, value.getBytes(CHARSET));
    }

    public static void encodeRespList(OutputStream output, List value)
            throws IOException
    {
        output.write(PREFIX_ARRAY);
        output.write(Integer.toString(value.size()).getBytes(Charsets.US_ASCII));
        for (Object item : value) {
            encodeResp(output, item);
        }
    }

    public static void encodeResp(OutputStream output, Object value)
            throws IOException
    {
        if (value == null) {
            output.write(NULL_BULK_STRING);
        }
        else if (value instanceof Byte) {
            encodeRespByte(output, (byte) value);
        }
        else if (value instanceof Short) {
            encodeRespShort(output, (short) value);
        }
        else if (value instanceof Integer) {
            encodeRespInt(output, (int) value);
        }
        else if (value instanceof Long) {
            encodeRespLong(output, (long) value);
        }
        else if (value instanceof byte[]) {
            encodeRespBytes(output, (byte[]) value);
        }
        else if (value instanceof String) {
            encodeRespString(output, (String) value);
        }
        else if (value instanceof List) {
            encodeRespList(output, (List) value);
        }
        else {
            throw new IllegalArgumentException(Objects.toString(value));
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
                        nextByte();
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

            private String readLineAsciiString()
                    throws IOException
            {
                OpenByteArrayOutputStream bos = readLineByteArrayOutputStream();
                return new String(bos.getBuf(), 0, bos.size(), Charsets.US_ASCII);
            }

            private void readSuffix()
                    throws IOException
            {
                for (byte b : SUFFIX) {
                    checkState(nextByte() == b);
                }
            }

            @Override
            public Object next()
            {
                try {
                    byte prefix = nextByte();

                    switch (prefix) {
                        case PREFIX_SIMPLE_STRING: {
                            return readLineBytes();
                        }
                        case PREFIX_ERROR: {
                            return new Error(readLineString());
                        }
                        case PREFIX_INTEGER: {
                            return Long.parseLong(readLineAsciiString());
                        }
                        case PREFIX_BULK_STRING: {
                            int length = Integer.parseInt(readLineAsciiString());
                            if (length == -1) {
                                return null;
                            }
                            byte[] buf = new byte[length];
                            if (hasPeekByte) {
                                buf[0] = nextByte();
                                int read = input.read(buf, 1, length - 1);
                                checkState(read == length - 1);
                            }
                            else {
                                int read = input.read(buf);
                                checkState(read == length);
                            }
                            readSuffix();
                            return buf;
                        }
                        case PREFIX_ARRAY: {
                            int length = Integer.parseInt(readLineAsciiString());
                            ArrayList<Object> lst = new ArrayList<>();
                            lst.ensureCapacity(length);
                            for (int i = 0; i < length; ++i) {
                                lst.add(next());
                            }
                            return lst;
                        }
                        default:
                            throw new IllegalStateException(Objects.toString(prefix));
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
        for (String str : new String[] {
                "+OK\r\n",
                "$6\r\nfoobar\r\n",
                "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n",
                "*3\r\n:1\r\n:2\r\n:3\r\n",
        }) {
            List<Object> lst = ImmutableList.copyOf(decodeResp(new BufferedInputStream(new ByteArrayInputStream(str.getBytes()))));
            System.out.println(lst);
        }
    }
}
