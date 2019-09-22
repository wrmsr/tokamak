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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.plan.node.NodeId;

import java.util.Optional;
import java.util.UUID;

public final class QueueEntry
{
    private final int seq;
    private final NodeId lockNodeId;
    private final Id lockId;
    private final UUID uuid;
    private final boolean force;
    private final boolean awaited;
    private final Optional<UUID> cycleTaskUuid;
    private final int numInvalidatedNodes;
    private final int numInvalidatedId;
    private final QueueInvalidations invalidations;

    public QueueEntry(
            int seq,
            NodeId lockNodeId,
            Id lockId,
            UUID uuid,
            boolean force,
            boolean awaited,
            Optional<UUID> cycleTaskUuid,
            int numInvalidatedNodes,
            int numInvalidatedId,
            QueueInvalidations invalidations)
    {
        this.seq = seq;
        this.lockNodeId = lockNodeId;
        this.lockId = lockId;
        this.uuid = uuid;
        this.force = force;
        this.awaited = awaited;
        this.cycleTaskUuid = cycleTaskUuid;
        this.numInvalidatedNodes = numInvalidatedNodes;
        this.numInvalidatedId = numInvalidatedId;
        this.invalidations = invalidations;
    }

    public int getSeq()
    {
        return seq;
    }

    public NodeId getLockNodeId()
    {
        return lockNodeId;
    }

    public Id getLockId()
    {
        return lockId;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public boolean isForce()
    {
        return force;
    }

    public boolean isAwaited()
    {
        return awaited;
    }

    public Optional<UUID> getCycleTaskUuid()
    {
        return cycleTaskUuid;
    }

    public int getNumInvalidatedNodes()
    {
        return numInvalidatedNodes;
    }

    public int getNumInvalidatedId()
    {
        return numInvalidatedId;
    }

    public QueueInvalidations getInvalidations()
    {
        return invalidations;
    }
}
