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
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class NopStateStorageImpl
        implements StateStorage
{
    @Override
    public void setup()
            throws IOException
    {
    }

    @Override
    public Optional<State> get(
            Connection conn,
            Id id,
            boolean create,
            boolean linkage,
            boolean attributes,
            boolean share,
            boolean noLock)
            throws IOException
    {
        return Optional.empty();
    }

    @Override
    public State createPhantom(StatefulNode node, Id id, Row row)
            throws IOException
    {
        return null;
    }

    @Override
    public void upgradePhantom(
            Connection conn,
            State state,
            boolean linkage,
            boolean share)
            throws IOException
    {
    }

    @Override
    public void put(
            Connection conn,
            State state,
            boolean create)
            throws IOException
    {
    }

    @Override
    public void putMany(
            Connection conn,
            List<State> states,
            boolean create)
            throws IOException
    {
    }

    @Override
    public List<Id> getSpanIds(Span<Id> span, OptionalInt limit)
            throws IOException
    {
        return null;
    }
}
