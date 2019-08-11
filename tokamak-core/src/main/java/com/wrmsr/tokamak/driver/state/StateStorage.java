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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.Span;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public interface StateStorage
{
    void setup()
            throws IOException;

    Map<StatefulNode, Map<Id, State>> get(
            StateStorageContext ctx,
            Map<StatefulNode, Set<Id>> idSetsByNode,
            boolean create,
            boolean linkage,
            boolean attributes,
            boolean share,
            boolean noLock
    )
            throws IOException;

    void put(
            StateStorageContext ctx,
            List<State> states,
            boolean create
    )
            throws IOException;

    State createPhantom(
            StateStorageContext ctx,
            StatefulNode node,
            Id id,
            Row row
    )
            throws IOException;

    void upgradePhantom(
            StateStorageContext ctx,
            State state,
            boolean linkage,
            boolean share
    )
            throws IOException;

    void allocate(
            StateStorageContext ctx,
            StatefulNode node,
            Iterable<Id> ids
    )
            throws IOException;

    List<Id> getSpanIds(
            StateStorageContext ctx,
            StatefulNode node,
            Span<Id> span,
            OptionalInt limit)
            throws IOException;
}
