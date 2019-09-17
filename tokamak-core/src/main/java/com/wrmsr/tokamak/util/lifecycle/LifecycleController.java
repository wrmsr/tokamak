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
    private volatile LifecycleState state = LifecycleState.NEW;
    private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();

    public LifecycleController(LifecycleComponent component)
    {
        this.component = checkNotNull(component);
    }

    @Override
    public String toString()
    {
        return "LifecycleController{" +
                "component=" + component +
                ", state=" + state +
                '}';
    }

    public LifecycleComponent getComponent()
    {
        return component;
    }

    public final LifecycleState getState()
    {
        return state;
    }

    public void addListener(LifecycleListener listener)
    {
        checkNotNull(listener);
        listeners.add(listener);
    }

    @Override
    public final void construct()
            throws Exception
    {
        synchronized (lock) {
            checkState(state == LifecycleState.NEW);
            state = LifecycleState.CONSTRUCTING;
            try {
                component.construct();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_CONSTRUCTING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.CONSTRUCTED;
        }
    }

    @Override
    public final void start()
            throws Exception
    {
        synchronized (lock) {
            checkState(state == LifecycleState.CONSTRUCTED);
            state = LifecycleState.STARTING;
            listeners.forEach(LifecycleListener::onStarting);
            try {
                component.start();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_STARTING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.STARTED;
            listeners.forEach(LifecycleListener::onStarted);
        }
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
                component.stop();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_STOPPING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.STOPPED;
            listeners.forEach(LifecycleListener::onStopped);
        }
    }

    @Override
    public final void destroy()
            throws Exception
    {
        synchronized (lock) {
            checkState(state != LifecycleState.DESTROYING &&
                    state != LifecycleState.FAILED_DESTROYING &&
                    state != LifecycleState.DESTROYED);
            state = LifecycleState.DESTROYING;
            try {
                component.destroy();
            }
            catch (Exception e) {
                state = LifecycleState.FAILED_DESTROYING;
                throw new RuntimeException(e);
            }
            state = LifecycleState.DESTROYED;
        }
    }
}
