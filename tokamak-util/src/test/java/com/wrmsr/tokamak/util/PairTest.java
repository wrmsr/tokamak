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

import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

public class PairTest
        extends TestCase
{
    public void testJson()
            throws Throwable
    {
        Pair<String, Integer> p0 = Pair.immutable("a", 0);
        String json = Json.writeValue(p0);
        System.out.println(json);
        Pair<?, ?> p1 = Json.readValue(json, Pair.Immutable.class);
        assertEquals(p0, p1);
    }
}
