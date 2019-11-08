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

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class LifecycleModule
        implements Module, ProvisionListener
{
    private final static class State
    {
        private final Binding<?> binding;
        private final Set<Lifecycle> dependencies = new HashSet<>();

        public State(Binding<?> binding)
        {
            this.binding = checkNotNull(binding);
        }
    }

    private final ThreadLocal<ArrayDeque<State>> stack = ThreadLocal.withInitial(ArrayDeque::new);
    private final LifecycleManager lifecycleManager = new LifecycleManager();

    public LifecycleManager getLifecycleManager()
    {
        return lifecycleManager;
    }

    @Override
    public void configure(Binder binder)
    {
        binder.bindListener(Matchers.any(), this);
        binder.bind(LifecycleManager.class).toInstance(lifecycleManager);
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> invocation)
    {
        Class<?> rawType = invocation.getBinding().getKey().getTypeLiteral().getRawType();
        if (!Lifecycle.class.isAssignableFrom(rawType)) {
            invocation.provision();
            return;
        }
        ArrayDeque<State> stack = this.stack.get();
        State prev = stack.peekFirst();
        State cur = new State(invocation.getBinding());
        stack.push(cur);
        try {
            Lifecycle lifecycle = (Lifecycle) invocation.provision();
            if (lifecycle != lifecycleManager) {
                try {
                    lifecycleManager.add(lifecycle, cur.dependencies);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (prev != null) {
                    prev.dependencies.add(lifecycle);
                }
            }
        }
        finally {
            stack.pop();
        }
    }
}
