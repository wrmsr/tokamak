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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.driver.state.State;
import com.wrmsr.tokamak.driver.state.StateStorageContext;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface StateCache
{
    boolean contains(State state);

    void invalidate(StatefulNode node, Set<Id> ids);

    boolean isInvalidated(StatefulNode node, Id id);

    State setPhantomAttributes(StatefulNode node, Id id, Optional<Row> row);

    void upgradePhantom(StateStorageContext storageCtx, State state, boolean share);

    Collection<State> get();

    Map<Node, Set<Id>> getInvalid();

    Optional<Pair.Immutable<Node, Id>> getNextInvalid();

    Map<Id, State> get(StatefulNode node);

    void flush(StateStorageContext storageCtx);
}
