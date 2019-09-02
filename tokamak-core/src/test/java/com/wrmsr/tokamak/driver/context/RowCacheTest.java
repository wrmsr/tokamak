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
package com.wrmsr.tokamak.driver.context;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.LineagePolicy;
import com.wrmsr.tokamak.driver.context.diag.Stat;
import com.wrmsr.tokamak.driver.context.row.RowCache;
import com.wrmsr.tokamak.driver.context.row.DefaultRowCache;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.type.Type;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

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

    public void testRowCacheImpl()
            throws Throwable
    {
        ScanNode scanNode = new ScanNode(
                "scan",
                SchemaTable.of("s", "t"),
                ImmutableMap.of("id", Type.LONG),
                ImmutableSet.of("id"),
                ImmutableSet.of(),
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        RowCache rc = new DefaultRowCache(
                Stat.Updater.nop());

        Optional<Collection<DriverRow>> rows = rc.get(scanNode, Key.of("id", 420L));

        rc.put(scanNode, Key.of("id", 420L), ImmutableList.of(
                new DriverRow(
                        scanNode,
                        LineagePolicy.NOP.build(),
                        Id.of(420L),
                        new Object[] {420L})
        ));
    }
}
