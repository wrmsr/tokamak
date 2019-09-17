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

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.wrmsr.tokamak.util.inject.advice.AdvisesBinder;
import junit.framework.TestCase;

import javax.inject.Inject;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.lifecycle.Lifecycles.runLifecycle;

public class LifecycleTest
        extends TestCase
{
    public static class A
            implements LifecycleComponent
    {

    }

    public static class B
            extends AbstractLifecycleComponent
    {

    }

    public void testLifecycle()
            throws Throwable
    {
        LifecycleManager lm = new LifecycleManager();
        A a = new A();
        B b = new B();

        lm.add(b, ImmutableList.of(a));

        runLifecycle(lm, () -> {
            assertEquals(LifecycleState.STARTED, lm.getState());
            assertTrue(lm.isStarted());
            assertEquals(LifecycleState.STARTED, b.getLifecycleState());
            assertEquals(LifecycleState.STARTED, lm.getState(a));
        });
    }

    public static class I
            extends AbstractLifecycleComponent
    {
        @Inject
        public I()
        {
        }
    }

    public static class J
            extends AbstractLifecycleComponent
    {
        @Inject
        public J(I i)
        {
        }
    }

    public void testGuice()
            throws Throwable
    {
        Injector inj = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                // bind(I.class);)
                bindListener(
                        Matchers.any(),
                        new TypeListener()
                        {
                            @Override
                            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter)
                            {
                                encounter.register(new InjectionListener<I>()
                                {
                                    @Override
                                    public void afterInjection(I injectee)
                                    {
                                        // TODO: get what was injected
                                        System.out.println(injectee);
                                    }
                                });
                            }
                        });
            }
        });

        J j = inj.getInstance(J.class);
    }

    static class RegisterLifecycle<T extends LifecycleComponent>
            implements UnaryOperator<T>
    {
        @Override
        public T apply(T component)
        {
            return component;
        }
    }

    public void testGuice2()
            throws Throwable
    {
        Injector inj = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                AdvisesBinder.bind(binder(), I.class);
                AdvisesBinder.bindAdvice(binder(), I.class, 0).to(new TypeLiteral<RegisterLifecycle<I>>() {});
            }
        });

        J j = inj.getInstance(J.class);
    }

    public static final class LifecycleDependencyTracker
    {
        private final Object lock = new Object();
        private final Map<LifecycleComponent, Set<LifecycleComponent>> dependencySetsByDependant = new HashMap<>();

        public void track(LifecycleComponent dependant, LifecycleComponent dependency)
        {
            synchronized (lock) {
                dependencySetsByDependant.computeIfAbsent(dependant, c -> new HashSet<>()).add(dependency);
            }
        }
    }

    public static class LifecycleRecordingModule
            extends AbstractModule
            implements ProvisionListener
    {
        private final static class State
        {
            private final Binding<?> binding;
            private final Set<LifecycleComponent> dependencies = new HashSet<>();

            public State(Binding<?> binding)
            {
                this.binding = checkNotNull(binding);
            }
        }

        private final ThreadLocal<ArrayDeque<State>> stack = ThreadLocal.withInitial(ArrayDeque::new);
        private final LifecycleDependencyTracker dependencyTracker = new LifecycleDependencyTracker();

        public LifecycleDependencyTracker getDependencyTracker()
        {
            return dependencyTracker;
        }

        @Override
        protected void configure()
        {
            bindListener(Matchers.any(), this);
            bind(LifecycleDependencyTracker.class).toInstance(dependencyTracker);
        }

        @Override
        public <T> void onProvision(ProvisionInvocation<T> invocation)
        {
            Class rawType = invocation.getBinding().getKey().getTypeLiteral().getRawType();
            if (!LifecycleComponent.class.isAssignableFrom(rawType)) {
                invocation.provision();
                return;
            }
            ArrayDeque<State> stack = this.stack.get();
            State prev = stack.peekFirst();
            State cur = new State(invocation.getBinding());
            stack.push(cur);
            try {
                LifecycleComponent component = (LifecycleComponent) invocation.provision();
                if (prev != null) {
                    prev.dependencies.add(component);
                }
            }
            finally {
                stack.pop();
            }
        }
    }

    public void testGuice3()
            throws Throwable
    {
        Injector inj = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install(new LifecycleRecordingModule());
                bind(I.class);
                bind(J.class);
            }
        });

        J j = inj.getInstance(J.class);
    }
}
