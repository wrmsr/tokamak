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

package com.wrmsr.tokamak.jdbc;

import com.wrmsr.tokamak.driver.queue.QueueEntry;
import com.wrmsr.tokamak.driver.queue.QueueInsertion;
import com.wrmsr.tokamak.driver.queue.QueueStorage;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbcQueueStorage
        implements QueueStorage
{
    private final Jdbi jdbi;

    public JdbcQueueStorage(Jdbi jdbi)
    {
        this.jdbi = checkNotNull(jdbi);
    }

    static class ContextImpl
            implements QueueStorage.Context
    {
        final Handle handle;

        ContextImpl(Handle handle)
        {
            this.handle = checkNotNull(handle);
        }

        @Override
        public void close()
                throws Exception
        {
            handle.close();
        }
    }

    @Override
    public Context createContext()
    {
        return new ContextImpl(jdbi.open());
    }

    @Override
    public Optional<List<QueueEntry>> insert(Context context, Iterable<QueueInsertion> insertions, boolean returnEntries, boolean coalesce)
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
