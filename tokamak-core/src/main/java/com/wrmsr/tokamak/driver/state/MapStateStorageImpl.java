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
package com.wrmsr.tokamak.driver.state;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.Span;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public class MapStateStorageImpl
        implements StateStorage
{
    private final Map<StatefulNode, Map<Id, State>> statesByIdByNode = new HashMap<>();

    @Override
    public void setup()
            throws IOException
    {
    }

    @Override
    public Context createContext()
    {
        return new Context() {};
    }

    @Override
    public Map<StatefulNode, Map<Id, State>> get(Context ctx, Map<StatefulNode, Set<Id>> idSetsByNode, EnumSet<GetFlag> flags)
            throws IOException
    {
        ImmutableMap.Builder<StatefulNode, Map<Id, State>> ret = ImmutableMap.builder();
        for (Map.Entry<StatefulNode, Set<Id>> entry : idSetsByNode.entrySet()) {
            StatefulNode node = entry.getKey();
            Map<Id, State> nodeMap = statesByIdByNode.computeIfAbsent(node, n -> new HashMap<>());
            Map<Id, State> retMap = new LinkedHashMap<>();

            for (Id id : entry.getValue()) {
                State state = nodeMap.get(id);

                if (state == null && flags.contains(GetFlag.CREATE)) {
                    state = new State(
                            new StateContext(node),
                            id,
                            State.Mode.INVALID);
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
    public void put(Context ctx, List<State> states, boolean create)
            throws IOException
    {
        for (State state : states) {
            Map<Id, State> nodeMap = statesByIdByNode.computeIfAbsent(state.getNode(), n -> new HashMap<>());
            if (create || nodeMap.containsKey(state.getId())) {
                nodeMap.put(state.getId(), state);
            }
        }
    }

    @Override
    public State createPhantom(Context ctx, StatefulNode node, Id id, DriverRow row)
            throws IOException
    {
        // return new State(
        //         new StateContext(node),
        //
        // )
        throw new IllegalStateException();
    }

    @Override
    public void upgradePhantom(Context ctx, State state, boolean linkage, boolean share)
            throws IOException
    {
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
