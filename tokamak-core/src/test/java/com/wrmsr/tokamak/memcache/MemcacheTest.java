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
package com.wrmsr.tokamak.memcache;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.io.CrLfByteReader;
import com.wrmsr.tokamak.util.collect.StreamableIterable;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class MemcacheTest
        extends TestCase
{
    public static final byte[] CRLF = new byte[] {'\r', '\n'};

    public static final String ERROR = "ERROR";
    public static final String CLIENT_ERROR = "CLIENT_ERROR";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    public static final String END = "END";
    public static final String VALUE = "VALUE";

    public static final int MAX_RELATIVE_TTL_S = 60 * 60 * 24 * 30;

    public static final class GetResponse
            implements StreamableIterable<GetResponse.Value>
    {
        public static final class Value
        {
            private final String key;
            private final int flags;
            private final int length;
            private final OptionalLong casUnique;
            private final byte[] data;

            public Value(String key, int flags, int length, OptionalLong casUnique, byte[] data)
            {
                this.key = checkNotNull(key);
                this.flags = flags;
                this.length = length;
                this.casUnique = checkNotNull(casUnique);
                this.data = checkNotNull(data);
            }

            public String getKey()
            {
                return key;
            }

            public int getFlags()
            {
                return flags;
            }

            public int getLength()
            {
                return length;
            }

            public OptionalLong getCasUnique()
            {
                return casUnique;
            }

            public byte[] getData()
            {
                return data;
            }
        }

        private final List<Value> values;

        public GetResponse(List<Value> values)
        {
            this.values = ImmutableList.copyOf(values);
        }

        public List<Value> getValues()
        {
            return values;
        }

        @Override
        public Iterator<Value> iterator()
        {
            return values.iterator();
        }

        public static GetResponse read(InputStream input)
                throws IOException
        {
            ImmutableList.Builder<Value> builder = ImmutableList.builder();
            CrLfByteReader crLfByteReader = new CrLfByteReader(input);
            loop:
            while (true) {
                String line = crLfByteReader.nextLineUtf8();
                Iterator<String> lineIt = Splitter.on(CharMatcher.whitespace()).split(line).iterator();
                checkState(lineIt.hasNext());
                String cmd = lineIt.next();
                switch (cmd) {
                    case VALUE:
                        String key = lineIt.next();
                        int flags = Integer.parseUnsignedInt(lineIt.next());
                        int length = Integer.parseInt(lineIt.next());
                        OptionalLong casUnique = lineIt.hasNext() ?
                                OptionalLong.of(Long.parseUnsignedLong(lineIt.next())) : OptionalLong.empty();
                        checkState(!lineIt.hasNext());
                        byte[] data = crLfByteReader.next(new byte[length]);
                        builder.add(
                                new Value(
                                        key,
                                        flags,
                                        length,
                                        casUnique,
                                        data));
                        break;
                    case END:
                        break loop;
                    default:
                        throw new IllegalStateException(cmd);
                }
            }
            return new GetResponse(builder.build());
        }
    }
}
