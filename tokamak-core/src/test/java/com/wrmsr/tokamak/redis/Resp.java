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
package com.wrmsr.tokamak.redis;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import com.wrmsr.tokamak.util.CrLfByteIterator;
import com.wrmsr.tokamak.util.box.Box;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class Resp
{
    /*
    https://redis.io/topics/protocol
    */

    public static final byte CR = '\r';
    public static final byte LF = '\n';
    public static final byte[] CRLF = new byte[] {CR, LF};

    public static final byte PREFIX_SIMPLE_STRING = '+';
    public static final byte PREFIX_ERROR = '-';
    public static final byte PREFIX_INTEGER = ':';
    public static final byte PREFIX_BULK_STRING = '$';
    public static final byte PREFIX_ARRAY = '*';

    public static final byte[] NULL_BULK_STRING = new byte[] {'$', '-', '1', '\r', '\n'};

    private Resp()
    {
    }

    public static void encodeNull(OutputStream output)
            throws IOException
    {
        output.write(NULL_BULK_STRING);
    }

    public static void encodeByte(OutputStream output, byte value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Byte.toString(value).getBytes(Charsets.US_ASCII));
        output.write(CRLF);
    }

    public static void encodeShort(OutputStream output, short value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Short.toString(value).getBytes(Charsets.US_ASCII));
        output.write(CRLF);
    }

    public static void encodeInt(OutputStream output, int value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Integer.toString(value).getBytes(Charsets.US_ASCII));
        output.write(CRLF);
    }

    public static void encodeLong(OutputStream output, long value)
            throws IOException
    {
        output.write(PREFIX_INTEGER);
        output.write(Long.toString(value).getBytes(Charsets.US_ASCII));
        output.write(CRLF);
    }

    public static void encodeBytes(OutputStream output, byte[] value)
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
        output.write(CRLF);
    }

    public static void encodeString(OutputStream output, String value)
            throws IOException
    {
        encodeBytes(output, value.getBytes(Charsets.UTF_8));
    }

    public static void encodeList(OutputStream output, List value)
            throws IOException
    {
        output.write(PREFIX_ARRAY);
        output.write(Integer.toString(value.size()).getBytes(Charsets.US_ASCII));
        for (Object item : value) {
            encode(output, item);
        }
    }

    public static void encode(OutputStream output, Object value)
            throws IOException
    {
        if (value == null) {
            output.write(NULL_BULK_STRING);
        }
        else if (value instanceof Byte) {
            encodeByte(output, (byte) value);
        }
        else if (value instanceof Short) {
            encodeShort(output, (short) value);
        }
        else if (value instanceof Integer) {
            encodeInt(output, (int) value);
        }
        else if (value instanceof Long) {
            encodeLong(output, (long) value);
        }
        else if (value instanceof byte[]) {
            encodeBytes(output, (byte[]) value);
        }
        else if (value instanceof String) {
            encodeString(output, (String) value);
        }
        else if (value instanceof List) {
            encodeList(output, (List) value);
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

    public static Iterator<Object> decode(InputStream input)
    {
        CrLfByteIterator crLfByteIterator = new CrLfByteIterator(input);
        return new Iterator<Object>()
        {
            @Override
            public boolean hasNext()
            {
                return crLfByteIterator.available() > 0;
            }

            @Override
            public Object next()
            {
                try {
                    byte prefix = crLfByteIterator.next();

                    switch (prefix) {
                        case PREFIX_SIMPLE_STRING: {
                            return crLfByteIterator.nextLine();
                        }
                        case PREFIX_ERROR: {
                            return new Error(crLfByteIterator.nextLineUtf8());
                        }
                        case PREFIX_INTEGER: {
                            return Long.parseLong(crLfByteIterator.nextLineAscii());
                        }
                        case PREFIX_BULK_STRING: {
                            int length = Integer.parseInt(crLfByteIterator.nextLineAscii());
                            if (length == -1) {
                                return null;
                            }
                            byte[] buf = crLfByteIterator.next(new byte[length]);
                            crLfByteIterator.nextBlankLine();
                            return buf;
                        }
                        case PREFIX_ARRAY: {
                            int length = Integer.parseInt(crLfByteIterator.nextLineAscii());
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
}
