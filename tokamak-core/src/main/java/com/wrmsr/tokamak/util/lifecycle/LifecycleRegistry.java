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

public final class LifecycleRegistry
        extends AbstractLifecycleComponent
{
    /*
    TODO:
     - self-deps (including component depping registry itself)
     - injector integration
     - @PostConstruct / javax.inject interop?
     - LifecycleController parent/hierarchy, ensure 0 or 1 parent
    */

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
    private final Map<LifecycleComponent, Entry> entriesByComponent = new IdentityHashMap<>();

    public LifecycleState getState()
    {
        return getLifecycleState();
    }

    private static LifecycleController getController(LifecycleComponent component)
    {
        if (component instanceof LifecycleController) {
            return (LifecycleController) component;
        }
        else if (component instanceof AbstractLifecycleComponent) {
            return ((AbstractLifecycleComponent) component).getLifecycleController();
        }
        else {
            return new LifecycleController(component);
        }
    }

    public LifecycleState getState(LifecycleComponent component)
    {
        if (component == this) {
            return getState();
        }
        Entry entry = checkNotNull(entriesByComponent.get(component));
        return entry.controller.getState();
    }

    @GuardedBy("lock")
    private Entry addInternal(LifecycleComponent component, Iterable<LifecycleComponent> dependencies)
            throws Exception
    {
        checkState(getState().getPhase() < LifecycleState.STOPPING.getPhase() && !getState().isFailed());

        Entry entry = entriesByComponent.get(component);
        if (entry == null) {
            entry = new Entry(getController(component));
            entriesByComponent.put(component, entry);
        }

        for (LifecycleComponent dep : dependencies) {
            Entry depEntry = addInternal(dep, ImmutableSet.of());
            entry.dependencies.add(depEntry);
            depEntry.dependants.add(entry);
        }

        LifecycleController controller = entry.controller;
        checkState(controller.getState().getPhase() < LifecycleState.STOPPING.getPhase() && !controller.getState().isFailed());
        while (controller.getState().getPhase() < getState().getPhase()) {
            checkState(!controller.getState().isFailed());
            switch (controller.getState()) {
                case NEW:
                    controller.postConstruct();
                    break;
                case INITIALIZED:
                    controller.start();
                    break;
                default:
                    throw new IllegalStateException(controller.getState().toString());
            }
        }

        return entry;
    }

    public <T extends LifecycleComponent> T add(T component, Iterable<LifecycleComponent> dependencies)
            throws Exception
    {
        synchronized (lock) {
            addInternal(component, dependencies);
        }
        return component;
    }

    public <T extends LifecycleComponent> T add(T component)
            throws Exception
    {
        return add(component, ImmutableSet.of());
    }

    @Override
    protected void doPostConstruct()
            throws Exception
    {
        for (Entry e : entriesByComponent.values()) {
            e.controller.postConstruct();
        }
    }

    @Override
    protected void doStart()
            throws Exception
    {
        for (Entry e : entriesByComponent.values()) {
            e.controller.start();
        }
    }

    @Override
    protected void doStop()
            throws Exception
    {
        for (Entry e : entriesByComponent.values()) {
            e.controller.stop();
        }
    }

    @Override
    protected void doClose()
            throws Exception
    {
        for (Entry e : entriesByComponent.values()) {
            e.controller.close();
        }
    }
}
