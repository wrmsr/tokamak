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

import com.google.common.collect.ImmutableSet;

import javax.annotation.concurrent.GuardedBy;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class LifecycleManager
        extends AbstractLifecycle
{
    private class Entry
    {
        private final LifecycleController controller;
        private final Set<Entry> dependencies = new HashSet<>();
        private final Set<Entry> dependants = new HashSet<>();

        public Entry(LifecycleController controller)
        {
            this.controller = checkNotNull(controller);
        }

        @Override
        public String toString()
        {
            return "Entry{" +
                    "controller=" + controller +
                    '}';
        }
    }

    private final Object lock = new Object();
    private final Map<Lifecycle, Entry> entriesByLifecycle = new IdentityHashMap<>();

    public LifecycleState getState()
    {
        return getLifecycleState();
    }

    private static LifecycleController getController(Lifecycle lifecycle)
    {
        if (lifecycle instanceof LifecycleController) {
            return (LifecycleController) lifecycle;
        }
        else if (lifecycle instanceof AbstractLifecycle) {
            return ((AbstractLifecycle) lifecycle).getLifecycleController();
        }
        else {
            return new LifecycleController(lifecycle);
        }
    }

    public LifecycleState getState(Lifecycle lifecycle)
    {
        if (lifecycle == this) {
            return getState();
        }
        Entry entry = checkNotNull(entriesByLifecycle.get(lifecycle));
        return entry.controller.getState();
    }

    @GuardedBy("lock")
    private Entry addInternal(Lifecycle lifecycle, Iterable<Lifecycle> dependencies)
            throws Exception
    {
        checkState(getState().getPhase() < LifecycleState.STOPPING.getPhase() && !getState().isFailed());

        Entry entry = entriesByLifecycle.get(lifecycle);
        if (entry == null) {
            entry = new Entry(getController(lifecycle));
            entriesByLifecycle.put(lifecycle, entry);
        }

        for (Lifecycle dep : dependencies) {
            Entry depEntry = addInternal(dep, ImmutableSet.of());
            entry.dependencies.add(depEntry);
            depEntry.dependants.add(entry);
        }

        // FIXME: reverse for shutdown
        LifecycleController controller = entry.controller;
        checkState(controller.getState().getPhase() < LifecycleState.STOPPING.getPhase() && !controller.getState().isFailed());
        while (controller.getState().getPhase() < getState().getPhase()) {
            checkState(!controller.getState().isFailed());
            switch (controller.getState()) {
                case NEW:
                    controller.construct();
                    break;
                case CONSTRUCTED:
                    controller.start();
                    break;
                default:
                    throw new IllegalStateException(controller.getState().toString());
            }
        }

        return entry;
    }

    public <T extends Lifecycle> T add(T lifecycle, Iterable<Lifecycle> dependencies)
            throws Exception
    {
        synchronized (lock) {
            addInternal(lifecycle, dependencies);
        }
        return lifecycle;
    }

    public <T extends Lifecycle> T add(T lifecycle)
            throws Exception
    {
        return add(lifecycle, ImmutableSet.of());
    }

    @Override
    protected void doConstruct()
            throws Exception
    {
        for (Entry e : entriesByLifecycle.values()) {
            e.controller.construct();
        }
    }

    @Override
    protected void doStart()
            throws Exception
    {
        for (Entry e : entriesByLifecycle.values()) {
            e.controller.start();
        }
    }

    @Override
    protected void doStop()
            throws Exception
    {
        for (Entry e : entriesByLifecycle.values()) {
            e.controller.stop();
        }
    }

    @Override
    protected void doDestroy()
            throws Exception
    {
        for (Entry e : entriesByLifecycle.values()) {
            e.controller.destroy();
        }
    }
}
