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

package com.wrmsr.tokamak.driver.queue;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class NopQueueStorageImpl
        implements QueueStorage
{
    @Override
    public Context createContext()
    {
        return new Context() {};
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
        return new Dequeuer()
        {
            @Override
            public void close()
                    throws Exception
            {
            }

            @Override
            public Iterator<QueueEntry> iterator()
            {
                return ImmutableList.<QueueEntry>of().iterator();
            }
        };
    }
}
