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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableBiMap;
import junit.framework.TestCase;

import java.util.Map;

public class OrderPreservingImmutableMapTest
        extends TestCase
{
    public void testJson()
            throws Throwable
    {
        Map<String, Integer> map = new OrderPreservingImmutableMap<>(
                ImmutableBiMap.<String, Integer>builder()
                        .put("a", 1)
                        .put("b", 2)
                        .put("c", 3)
                        .build());

        String json = Json.toJson(map);
        System.out.println(json);
        Map<String, Integer> map2 = Json.OBJECT_MAPPER_THREAD_LOCAL.get().readValue(
                json, new TypeReference<OrderPreservingImmutableMap<String, Integer>>() {});
        System.out.println(map2);
    }
}
