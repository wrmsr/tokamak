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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Charsets;

import javax.annotation.concurrent.Immutable;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.StringPrefixing.stripPrefix;
import static com.wrmsr.tokamak.util.MoreBytes.fromHex;
import static com.wrmsr.tokamak.util.MoreBytes.toHex;

@Immutable
public final class Id
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

    public static Id of(long value)
    {
        return of(ByteBuffer.allocate(8).putLong(value).array());
    }

    public static Id of(String value)
    {
        checkNotNull(value);
        return of(value.getBytes(Charsets.UTF_8));
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
        return new String(value, Charsets.UTF_8);
    }

    public static String PREFIX = "id:";

    @JsonCreator
    public static Id parsePrefixed(String string)
    {
        return new Id(fromHex(stripPrefix(PREFIX, string)));
    }

    @JsonValue
    public String toPrefixedString()
    {
        return PREFIX + toHex(value);
    }
}
