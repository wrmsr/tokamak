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
package com.wrmsr.tokamak.core;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import junit.framework.TestCase;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class GuiceTest
        extends TestCase
{
    public static final class IntForwarder
            implements Supplier<Integer>
    {
        private final Integer value;

        @Inject
        public IntForwarder(Integer value)
        {
            this.value = value;
        }

        @Override
        public Integer get()
        {
            return value;
        }
    }

    public static final class IntForwarderModule
            extends PrivateModule
    {
        private final String name;
        private final Integer value;

        public IntForwarderModule(String name, Integer value)
        {
            this.name = name;
            this.value = value;
        }

        @Override
        protected void configure()
        {
            bind(Integer.class).toInstance(value);
            bind(new TypeLiteral<Supplier<Integer>>() {}).annotatedWith(Names.named(name)).to(IntForwarder.class);
            expose(new TypeLiteral<Supplier<Integer>>() {}).annotatedWith(Names.named(name));
        }
    }

    public void testThing()
            throws Throwable
    {
        Injector injector = Guice.createInjector(new IntForwarderModule("a", 1), new IntForwarderModule("b", 2));
        System.out.println(injector.getInstance(Key.get(new TypeLiteral<Supplier<Integer>>() {}, Names.named("a"))).get());
        System.out.println(injector.getInstance(Key.get(new TypeLiteral<Supplier<Integer>>() {}, Names.named("b"))).get());
    }

    public void testAmbiguousGenerics()
            throws Throwable
    {
        Injector injector = Guice.createInjector(
                new AbstractModule()
                {
                    @Override
                    protected void configure()
                    {
                        bind(new TypeLiteral<Map<String, Integer>>() {}).toInstance(ImmutableMap.of("a", 1));
                        bind(new TypeLiteral<Map<Integer, String>>() {}).toInstance(ImmutableMap.of(2, "b"));
                    }
                });

        System.out.println(injector.getInstance(Key.get(new TypeLiteral<Map<String, Integer>>() {})));
        System.out.println(injector.getInstance(Key.get(new TypeLiteral<Map<Integer, String>>() {})));

        // System.out.println(injector.getInstance(Key.get(new TypeLiteral<Map<Integer, Object>>() {})));
    }

    public void testPrivateOverrides()
            throws Throwable
    {
        Injector injectorBase = Guice.createInjector(
                new AbstractModule()
                {
                    @Override
                    protected void configure()
                    {
                        newSetBinder(binder(), Integer.class).addBinding().toInstance(-1);
                    }
                });

        System.out.println(injectorBase.getInstance(Key.get(new TypeLiteral<Set<Integer>>() {})));

        Injector injector0 = injectorBase.createChildInjector(
                new AbstractModule()
                {
                    @Override
                    protected void configure()
                    {
                        // bind(Integer.class).toInstance(0);
                        newSetBinder(binder(), Integer.class).addBinding().toInstance(0);
                    }
                });

        System.out.println(injector0.getInstance(Key.get(new TypeLiteral<Set<Integer>>() {})));

        Injector injector1 = injector0.createChildInjector(new PrivateModule()
        {
            @Override
            protected void configure()
            {
                newSetBinder(binder(), Integer.class).addBinding().toInstance(1);
            }
        });

        System.out.println(injector1.getInstance(Key.get(new TypeLiteral<Set<Integer>>() {})));

        Injector injector2 = injector0.createChildInjector(new PrivateModule()
        {
            @Override
            protected void configure()
            {
                newSetBinder(binder(), Integer.class).addBinding().toInstance(2);
            }
        });

        System.out.println(injector2.getInstance(Key.get(new TypeLiteral<Set<Integer>>() {})));
    }
}
