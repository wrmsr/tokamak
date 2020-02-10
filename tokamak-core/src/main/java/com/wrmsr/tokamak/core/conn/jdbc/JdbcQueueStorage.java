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

import com.wrmsr.tokamak.core.driver.queue.QueueEntry;
import com.wrmsr.tokamak.core.driver.queue.QueueInsertion;
import com.wrmsr.tokamak.core.driver.queue.QueueStorage;
import com.wrmsr.tokamak.util.sql.SqlConnection;
import com.wrmsr.tokamak.util.sql.SqlEngine;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbcQueueStorage
        implements QueueStorage
{
    private final SqlEngine sqlEngine;

    public JdbcQueueStorage(SqlEngine sqlEngine)
    {
        this.sqlEngine = checkNotNull(sqlEngine);
    }

    static class ContextImpl
            implements QueueStorage.Context
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
    public Context newContext()
    {
        return new ContextImpl(sqlEngine.connect());
    }

    @Override
    public Optional<List<QueueEntry>> enqueue(Context context, Iterable<QueueInsertion> insertions, boolean returnEntries, boolean coalesce)
            throws IOException
    {
        return Optional.empty();
    }

    @Override
    public Dequeuer createDequeuer(Context context)
    {
        return null;
    }
}
