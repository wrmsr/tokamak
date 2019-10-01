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
package com.wrmsr.tokamak.core.conn.jdbc;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.state.StateStorage;
import com.wrmsr.tokamak.core.driver.state.StorageState;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.util.Span;
import com.wrmsr.tokamak.util.sql.SqlConnection;
import com.wrmsr.tokamak.util.sql.SqlEngine;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbcStateStorage
        implements StateStorage
{
    /*
    create table state(
        id varbinary(255) not null primary key,
        version version not null,
        created_at datetime not null default current_timestamp,
        updated_at datetime not null default current_timestamp,
        attributes longblob,
        input longblob,
        output longblob
    );
    */

    private final SqlEngine sqlEngine;

    public JdbcStateStorage(SqlEngine sqlEngine)
    {
        this.sqlEngine = checkNotNull(sqlEngine);
    }

    static class ContextImpl
            implements Context
    {
        final SqlConnection sqlConnection;

        ContextImpl(SqlConnection sqlConnection)
        {
            this.sqlConnection = checkNotNull(sqlConnection);
        }

        @Override
        public void close()
        {
            sqlConnection.close();
        }
    }

    @Override
    public Context createContext()
    {
        return new ContextImpl(sqlEngine.connect());
    }

    @Override
    public void setup()
            throws IOException
    {
    }

    @Override
    public Map<PState, Map<Id, StorageState>> get(Context ctx, Map<PState, Set<Id>> idSetsByNode, EnumSet<GetFlag> flags)
            throws IOException
    {
        return null;
    }

    @Override
    public void put(Context ctx, List<StorageState> states, boolean create)
            throws IOException
    {

    }

    @Override
    public void allocate(Context ctx, PState node, Iterable<Id> ids)
            throws IOException
    {

    }

    @Override
    public List<Id> getSpanIds(Context ctx, PState node, Span<Id> span, OptionalInt limit)
            throws IOException
    {
        return null;
    }
}
