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
package com.wrmsr.tokamak.api;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.wrmsr.tokamak.api.Util.checkArgument;
import static com.wrmsr.tokamak.api.Util.checkNotNull;
import static com.wrmsr.tokamak.api.Util.checkState;
import static com.wrmsr.tokamak.api.Util.fromHex;
import static com.wrmsr.tokamak.api.Util.toHex;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Id
        implements Comparable<Id>
{
    private final byte[] value;

    public Id(byte[] value)
    {
        this.value = checkNotNull(value);
    }

    public byte[] getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Id id = (Id) o;
        return Arrays.equals(value, id.value);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString()
    {
        return "Id{" +
                "value=" + toHex(value) +
                '}';
    }

    @Override
    public int compareTo(Id o)
    {
        byte[] left = value;
        byte[] right = o.value;
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }

    public static Id of(long value)
    {
        return of(ByteBuffer.allocate(8).putLong(value).array());
    }

    public static Id of(String value)
    {
        checkNotNull(value);
        return of(value.getBytes(UTF_8));
    }

    public static Id of(byte[] value)
    {
        return new Id(value);
    }

    public long asLong()
    {
        checkState(value.length == 8);
        return ByteBuffer.wrap(value).getLong();
    }

    public String asString()
    {
        return new String(value, UTF_8);
    }

    public static String PREFIX = "id:";

    public static Id parsePrefixed(String string)
    {
        checkArgument(string.startsWith(PREFIX));
        return new Id(fromHex(string.substring(PREFIX.length())));
    }

    public String toPrefixedString()
    {
        return PREFIX + toHex(value);
    }

    public static final JsonConverter<Id, String> JSON_CONVERTER = new JsonConverter<>(
            Id.class,
            String.class,
            Id::toPrefixedString,
            Id::parsePrefixed);
}
