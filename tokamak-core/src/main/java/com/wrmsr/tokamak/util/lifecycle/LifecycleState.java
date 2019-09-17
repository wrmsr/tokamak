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
    NEW(false),

    INITIALIZING(false),
    FAILED_INITIALIZING(true),
    INITIALIZED(false),

    STARTING(false),
    FAILED_STARTING(true),
    STARTED(false),

    STOPPING(false),
    FAILED_STOPPING(true),
    STOPPED(false),

    CLOSING(false),
    FAILED_CLOSING(true),
    CLOSED(false),

    ;

    private final boolean isFailure;

    LifecycleState(boolean isFailure)
    {
        this.isFailure = isFailure;
    }

    public boolean isFailure()
    {
        return isFailure;
    }
}
