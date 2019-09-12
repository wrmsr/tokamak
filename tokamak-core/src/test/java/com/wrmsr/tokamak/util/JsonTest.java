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
package com.wrmsr.tokamak.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SimpleRow;
import junit.framework.TestCase;

import java.util.List;

public class JsonTest
        extends TestCase
{
    public void testApiJson()
            throws Throwable
    {
        System.out.println(
                Json.writeValue(
                        Key.all()));

        System.out.println(
                Json.writeValue(
                        new SimpleRow(
                                Id.of(420),
                                new Object[] {
                                        "hi",
                                        420,
                                        new byte[] {(byte) 0x01, (byte) 0x34}
                                })));

        Object obj = ImmutableMap.of(
                "a", 0,
                "b", "one",
                "c", ImmutableList.of("a", "b", "c"),
                "d", ImmutableMap.of(
                        "e", 420,
                        "f", ImmutableList.of(1, "a"),
                        "g", ImmutableMap.of(
                                0, "hi",
                                "h", "no"
                        )
                )
        );

        String blob = Json.writeValue(obj);

        JsonNode node = Json.OBJECT_MAPPER_SUPPLIER.get().readTree(blob);

        System.out.println(node);
    }

    public static class Link
    {
        public final @JsonProperty("name") String name;
        public final @JsonProperty("links") List<Link> links;

        @JsonCreator
        public Link(@JsonProperty("name") String name, @JsonProperty("links") List<Link> links)
        {
            this.name = name;
            this.links = links;
        }

        @Override
        public String toString()
        {
            return "Link@" + System.identityHashCode(this) + "{" +
                    "name='" + name + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    public void testReferenceJson()
            throws Throwable
    {
        Link a = new Link("a", ImmutableList.of());
        Link b = new Link("b", ImmutableList.of(a));
        Link c = new Link("c", ImmutableList.of(a));
        Link d = new Link("d", ImmutableList.of(b, c));

        System.out.println(d);

        String src =Json.writeValue(d);
        System.out.println(src);

        Link jl = Json.readValue(src, Link.class);
        System.out.println(jl);
    }
}
