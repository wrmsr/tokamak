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
    private class Entry
    {
        private final LifecycleController controller;
        private final Set<LifecycleController> dependencies = new HashSet<>();

        public Entry(LifecycleController controller)
        {
            this.controller = checkNotNull(controller);
        }
    }

    private final Object lock = new Object();
    private final Map<LifecycleComponent, Entry> entriesByComponent = new IdentityHashMap<>();

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

    @GuardedBy("lock")
    private Entry addInternal(LifecycleComponent component, Set<LifecycleComponent> dependencies)
    {
        checkState(getLifecycleState() == LifecycleState.INITIALIZING);
        Entry entry = entriesByComponent.computeIfAbsent(component, c -> new Entry(getController(c)));
        dependencies.forEach(d -> addInternal(d, ImmutableSet.of()));
        return entry;
    }

    public void add(LifecycleComponent component, Set<LifecycleComponent> dependencies)
    {
        synchronized (lock) {
            addInternal(component, dependencies);
        }
    }

    public void add(LifecycleComponent component)
    {
        add(component, ImmutableSet.of());
    }
}
