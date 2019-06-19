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
package com.wrmsr.tokamak.materialization.driver.state;

import com.wrmsr.tokamak.materialization.api.Attributes;
import com.wrmsr.tokamak.materialization.api.Id;
import com.wrmsr.tokamak.materialization.node.StatefulNode;
import com.wrmsr.tokamak.util.Span;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface StateStorage
{
    void setup()
            throws IOException;

    Optional<State> get(
            Connection conn,
            Id id,
            boolean create,
            boolean linkage,
            boolean attributes,
            boolean share,
            boolean nolock
    )
            throws IOException;

    State createPhantom(
            StatefulNode node,
            Id id,
            Attributes attributes
    )
            throws IOException;

    void upgradePhantom(
            Connection conn,
            State state,
            boolean linkage,
            boolean share
    )
            throws IOException;

    void put(
            Connection conn,
            State state,
            boolean create
    )
            throws IOException;

    void putMany(
            Connection conn,
            List<State> states,
            boolean create
    )
            throws IOException;

    List<Id> getSpanIds(Span<Id> span, OptionalInt limit)
            throws IOException;
}
