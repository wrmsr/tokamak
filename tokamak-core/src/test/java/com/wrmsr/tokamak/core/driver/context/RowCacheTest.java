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
package com.wrmsr.tokamak.core.driver.context;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class RowCacheTest
        extends TestCase
{
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testKeys()
            throws Throwable
    {
        Map<ImmutableSet<String>, String> m = ImmutableMap.of(
                ImmutableSet.of("a", "b"), "ab",
                ImmutableSet.of("a", "c"), "ac"
        );

        assertEquals("ab", m.get(ImmutableSet.of("a", "b")));
        assertEquals("ab", m.get(ImmutableSet.of("b", "a")));
        assertEquals("ac", m.get(new HashSet()
        {{
            add("c");
            add("a");
        }}));

        Map<ImmutableList<String>, String> m2 = ImmutableMap.of(
                ImmutableList.of("a", "b"), "ab",
                ImmutableList.of("a", "c"), "ac"
        );

        assertEquals("ab", m2.get(ImmutableList.of("a", "b")));
        assertEquals("ab", m2.get(new ArrayList()
        {{
            add("a");
            add("b");
        }}));
        assertNull(m2.get(ImmutableList.of("b", "a")));
    }
}
