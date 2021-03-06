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
package com.wrmsr.tokamak.core.driver.queue;

import com.wrmsr.tokamak.util.NoExceptAutoCloseable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface QueueStorage
{
    interface Context
            extends NoExceptAutoCloseable
    {
    }

    Context newContext();

    Optional<List<QueueEntry>> enqueue(Context context, Iterable<QueueInsertion> insertions, boolean returnEntries, boolean coalesce)
            throws IOException;

    interface Dequeuer
            extends Iterable<QueueEntry>, NoExceptAutoCloseable
    {
    }

    Dequeuer createDequeuer(Context context);
}
