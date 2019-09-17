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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class LifecycleController
        implements LifecycleComponent
{
    private final LifecycleComponent component;

    private final Object lock = new Object();
    private volatile LifecycleState state;
    private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();

    public LifecycleController(LifecycleComponent component)
    {
        this.component = checkNotNull(component);
    }

    public LifecycleComponent getComponent()
    {
        return component;
    }

    public final LifecycleState getState()
    {
        return state;
    }

    @Override
    public final void postConstruct()
            throws Exception
    {
        synchronized (lock) {
            checkState(state == LifecycleState.NEW);
            state = LifecycleState.INITIALIZING;
            try {
                doPostConstruct();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_INITIALIZING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.INITIALIZED;
        }
    }

    protected void doPostConstruct()
            throws Exception
    {
    }

    @Override
    public final void start()
            throws Exception
    {
        synchronized (lock) {
            checkState(state == LifecycleState.INITIALIZED);
            state = LifecycleState.STARTING;
            listeners.forEach(LifecycleListener::onStarting);
            try {
                doStart();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_STARTING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.STARTED;
            listeners.forEach(LifecycleListener::onStart);
        }
    }

    protected void doStart()
            throws Exception
    {
    }

    @Override
    public final void stop()
            throws Exception
    {
        synchronized (lock) {
            checkState(state == LifecycleState.STARTED);
            state = LifecycleState.STOPPING;
            listeners.forEach(LifecycleListener::onStopping);
            try {
                doStop();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_STOPPING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.STOPPED;
            listeners.forEach(LifecycleListener::onStop);
        }
    }

    protected void doStop()
            throws Exception
    {
    }

    @Override
    public final void close()
            throws Exception
    {
        synchronized (lock) {
            checkState(state != LifecycleState.CLOSING &&
                    state != LifecycleState.FAILED_CLOSING &&
                    state != LifecycleState.CLOSED);
            state = LifecycleState.CLOSING;
            try {
                doClose();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_CLOSING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.CLOSED;
        }
    }

    protected void doClose()
            throws Exception
    {
    }
}
