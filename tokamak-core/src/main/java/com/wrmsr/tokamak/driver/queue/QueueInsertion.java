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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.plan.node.NodeId;

import java.util.Optional;
import java.util.UUID;

public final class QueueInsertion
{
    private final NodeId lockNodeId;
    private final Id lockId;
    private final QueueInvalidations invalidations;
    private final Optional<UUID> uuid;
    private final boolean force;
    private final boolean awaited;
    private final Optional<UUID> cycleTaskUuid;

    public QueueInsertion(
            NodeId lockNodeId,
            Id lockId,
            QueueInvalidations invalidations,
            Optional<UUID> uuid,
            boolean force,
            boolean awaited,
            Optional<UUID> cycleTaskUuid)
    {
        this.lockNodeId = lockNodeId;
        this.lockId = lockId;
        this.invalidations = invalidations;
        this.uuid = uuid;
        this.force = force;
        this.awaited = awaited;
        this.cycleTaskUuid = cycleTaskUuid;
    }

    public NodeId getLockNodeId()
    {
        return lockNodeId;
    }

    public Id getLockId()
    {
        return lockId;
    }

    public QueueInvalidations getInvalidations()
    {
        return invalidations;
    }

    public Optional<UUID> getUuid()
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
}
