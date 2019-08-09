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
import com.google.common.collect.Comparators;
import com.google.common.hash.Hashing;
import com.wrmsr.tokamak.util.box.IntBox;

import javax.annotation.concurrent.Immutable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.StringPrefixing.stripPrefix;

@Immutable
public final class NodeId
        extends IntBox
        implements Comparable<NodeId>
{
    private final String string;

    public NodeId(int value)
    {
        super(value);

        string = String.format("%08x", value);
    }

    @Override
    public String toString()
    {
        return string;
    }

    @Override
    public int compareTo(NodeId o)
    {
        return Integer.compare(value, o.value);
    }

    public static NodeId parse(String string)
    {
        checkNotNull(string);
        return new NodeId(Integer.parseInt(string, 16));
    }

    public static NodeId of(String name)
    {
        checkNotNull(name);
        ByteBuffer bytes = StandardCharsets.UTF_8.encode(name);
        int hash = Hashing.murmur3_32().newHasher().putBytes(bytes).hash().asInt();
        return new NodeId(hash);
    }

    public static final String PREFIX = "nodeid:";

    @JsonCreator
    public static NodeId parsePrefixed(String string)
    {
        return parse(stripPrefix(PREFIX, string));
    }

    @JsonValue
    public String toPrefixedString()
    {
        return PREFIX + toString();
    }
}
