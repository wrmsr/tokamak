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
package com.wrmsr.tokamak.materialization.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.hash.Hashing;
import com.wrmsr.tokamak.util.IntBox;

import javax.annotation.concurrent.Immutable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.wrmsr.tokamak.util.StringPrefixing.stripPrefix;

@Immutable
public final class NodeId
        extends IntBox
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

    public static NodeId parse(String string)
    {
        return new NodeId(Integer.parseInt(string, 16));
    }

    public static NodeId compute(NodeName name)
    {
        ByteBuffer bytes = StandardCharsets.UTF_8.encode(name.getValue());
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
