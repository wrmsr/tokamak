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

public enum LifecycleState
{
    NEW(0, false),

    INITIALIZING(1, false),
    FAILED_INITIALIZING(2, true),
    INITIALIZED(3, false),

    STARTING(5, false),
    FAILED_STARTING(6, true),
    STARTED(7, false),

    STOPPING(8, false),
    FAILED_STOPPING(9, true),
    STOPPED(10, false),

    CLOSING(11, false),
    FAILED_CLOSING(12, true),
    CLOSED(13, false),

    ;

    private final int phase;
    private final boolean isFailure;

    LifecycleState(int phase, boolean isFailure)
    {
        this.phase = phase;
        this.isFailure = isFailure;
    }

    public int getPhase()
    {
        return phase;
    }

    public boolean isFailure()
    {
        return isFailure;
    }
}
