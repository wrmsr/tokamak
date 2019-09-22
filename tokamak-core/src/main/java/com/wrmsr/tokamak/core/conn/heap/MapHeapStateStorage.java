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
package com.wrmsr.tokamak.core.conn.heap;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.state.StorageState;
import com.wrmsr.tokamak.core.plan.node.StatefulNode;
import com.wrmsr.tokamak.util.Span;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public class MapHeapStateStorage
        implements HeapStateStorage
{
    /*
    TODO:
     - locks
    */

    private final Map<StatefulNode, Map<Id, StorageState>> statesByIdByNode = new HashMap<>();

    @Override
    public Context createContext()
    {
        return new Context() {};
    }

    @Override
    public void setup()
            throws IOException
    {
    }

    @Override
    public Map<StatefulNode, Map<Id, StorageState>> get(Context ctx, Map<StatefulNode, Set<Id>> idSetsByNode, EnumSet<GetFlag> flags)
            throws IOException
    {
        ImmutableMap.Builder<StatefulNode, Map<Id, StorageState>> ret = ImmutableMap.builder();
        for (Map.Entry<StatefulNode, Set<Id>> entry : idSetsByNode.entrySet()) {
            StatefulNode node = entry.getKey();
            Map<Id, StorageState> nodeMap = statesByIdByNode.computeIfAbsent(node, n -> new HashMap<>());
            Map<Id, StorageState> retMap = new LinkedHashMap<>();

            for (Id id : entry.getValue()) {
                StorageState state = nodeMap.get(id);

                if (state == null && flags.contains(GetFlag.CREATE)) {
                    state = new StorageState(
                            node,
                            id,
                            StorageState.Mode.CREATED,
                            0L,
                            -1.0f,
                            -1.0f,
                            null,
                            0L,
                            null,
                            null,
                            0L);
                }

                if (state != null) {
                    retMap.put(id, state);
                }
            }

            if (!retMap.isEmpty()) {
                ret.put(entry.getKey(), retMap);
            }
        }
        return ret.build();
    }

    @Override
    public void put(Context ctx, List<StorageState> states, boolean create)
            throws IOException
    {
        for (StorageState state : states) {
            Map<Id, StorageState> nodeMap = statesByIdByNode.computeIfAbsent(state.getNode(), n -> new HashMap<>());
            if (create || nodeMap.containsKey(state.getId())) {
                nodeMap.put(state.getId(), state);
            }
        }
    }

    @Override
    public void allocate(Context ctx, StatefulNode node, Iterable<Id> ids)
            throws IOException
    {
    }

    @Override
    public List<Id> getSpanIds(Context ctx, StatefulNode node, Span<Id> span, OptionalInt limit)
            throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
