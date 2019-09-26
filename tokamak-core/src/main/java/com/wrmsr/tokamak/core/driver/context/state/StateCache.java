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
package com.wrmsr.tokamak.core.driver.context.state;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.StateNode;
import com.wrmsr.tokamak.util.Pair;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface StateCache
{
    enum GetFlag
    {
        CREATE,
        INVALIDATE,
        NOLOAD,
    }

    Optional<State> get(StateNode node, Id id, EnumSet<GetFlag> flags);

    boolean contains(State state);

    void invalidate(StateNode node, Set<Id> ids);

    boolean isInvalidated(StateNode node, Id id);

    State createPhantom(StateNode node, Row row);

    State setPhantomAttributes(StateNode node, Row row);

    Collection<State> getAll();

    Map<Node, Set<Id>> getInvalid();

    Optional<Pair.Immutable<Node, Id>> getNextInvalid();

    Map<Id, State> getIdMap(StateNode node);

    void flush();
}
