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
package com.wrmsr.tokamak.util;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.wrmsr.tokamak.util.lifecycle.AbstractLifecycleComponent;
import com.wrmsr.tokamak.util.lifecycle.LifecycleComponent;
import com.wrmsr.tokamak.util.lifecycle.LifecycleManager;
import com.wrmsr.tokamak.util.lifecycle.LifecycleState;
import junit.framework.TestCase;

import javax.inject.Inject;

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
    {
        @Inject
        public I()
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
        I i = inj.getInstance(I.class);
    }
}
