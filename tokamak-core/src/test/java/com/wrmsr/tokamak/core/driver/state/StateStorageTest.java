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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.conn.heap.MapHeapStateStorage;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.PNodeAnnotations;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.type.Types;
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
        PScan scanNode = new PScan(
                "scan",
                PNodeAnnotations.empty().mapFields(fields -> fields.with("id", FieldAnnotation.id())),
                SchemaTable.of("s", "t"),
                ImmutableMap.of("id", Types.LONG),
                ImmutableSet.of("id"),
                ImmutableSet.of());

        PState stateNode = new PState(
                "state",
                PNodeAnnotations.empty(),
                scanNode,
                ImmutableList.of(),
                PState.Denormalization.NONE,
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        StateStorage ss = new MapHeapStateStorage();
        Map<PState, Map<Id, StorageState>> map = ss.get(
                ss.createContext(),
                ImmutableMap.of(stateNode, ImmutableSet.of(Id.of(420))),
                EnumSet.of(StateStorage.GetFlag.CREATE));

        System.out.println(map);
    }
}
