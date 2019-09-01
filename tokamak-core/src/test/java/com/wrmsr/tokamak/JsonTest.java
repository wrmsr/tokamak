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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SimpleRow;
import com.wrmsr.tokamak.util.Json;
import junit.framework.TestCase;

public class JsonTest
        extends TestCase
{
    public void testAPiJson()
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
}
