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
package com.wrmsr.tokamak.util.lifecycle;

import static com.google.common.base.Preconditions.checkState;

public enum LifecycleState
{
    NEW(0, false),

    CONSTRUCTING(1, false),
    FAILED_CONSTRUCTING(2, true),
    CONSTRUCTED(3, false),

    STARTING(5, false),
    FAILED_STARTING(6, true),
    STARTED(7, false),

    STOPPING(8, false),
    FAILED_STOPPING(9, true),
    STOPPED(10, false),

    DESTROYING(11, false),
    FAILED_DESTROYING(12, true),
    DESTROYED(13, false),

    ;

    private final int phase;
    private final boolean isFailed;

    LifecycleState(int phase, boolean isFailed)
    {
        this.phase = phase;
        this.isFailed = isFailed;
    }

    public int getPhase()
    {
        return phase;
    }

    public boolean isFailed()
    {
        return isFailed;
    }

    public boolean isConstructed()
    {
        checkState(!isFailed);
        return phase >= CONSTRUCTED.phase;
    }

    public boolean isStarted()
    {
        checkState(!isFailed);
        return this == STARTED;
    }

    public boolean isStopped()
    {
        checkState(!isFailed);
        return this == STOPPED;
    }

    public boolean isDestroyed()
    {
        checkState(!isFailed);
        return this == DESTROYED;
    }
}
