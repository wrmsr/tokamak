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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ToposortTest
        extends TestCase
{
    public void testToposort()
    {
        Map<String, Set<String>> data = ImmutableMap.of(
                "A", ImmutableSet.of("C"),
                "B", ImmutableSet.of(),
                "C", ImmutableSet.of("B"),
                "D", ImmutableSet.of("B")
        );

        List<Set<String>> ts = MoreCollections.toposort(data);

        assertEquals(ts, ImmutableList.of(
                ImmutableSet.of("B"),
                ImmutableSet.of("C", "D"),
                ImmutableSet.of("A")
        ));
    }
}
