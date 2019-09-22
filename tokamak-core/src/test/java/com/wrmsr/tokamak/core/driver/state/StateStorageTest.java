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
package com.wrmsr.tokamak.core.driver.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.conn.heap.MapHeapStateStorage;
import com.wrmsr.tokamak.core.plan.node.ScanNode;
import com.wrmsr.tokamak.core.plan.node.StatefulNode;
import com.wrmsr.tokamak.core.type.Type;
import junit.framework.TestCase;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

public class StateStorageTest
        extends TestCase
{
    public void testMap()
            throws Throwable
    {
        StatefulNode scanNode = new ScanNode(
                "scan",
                SchemaTable.of("s", "t"),
                ImmutableMap.of("id", Type.LONG),
                ImmutableSet.of("id"),
                ImmutableSet.of(),
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        StateStorage ss = new MapHeapStateStorage();
        Map<StatefulNode, Map<Id, StorageState>> map = ss.get(
                ss.createContext(),
                ImmutableMap.of(scanNode, ImmutableSet.of(Id.of(420))),
                EnumSet.of(StateStorage.GetFlag.CREATE));

        System.out.println(map);
    }
}
