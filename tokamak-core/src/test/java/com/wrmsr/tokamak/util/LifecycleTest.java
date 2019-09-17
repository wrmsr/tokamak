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
import com.wrmsr.tokamak.util.lifecycle.AbstractLifecycleComponent;
import com.wrmsr.tokamak.util.lifecycle.LifecycleComponent;
import com.wrmsr.tokamak.util.lifecycle.LifecycleManager;
import com.wrmsr.tokamak.util.lifecycle.LifecycleState;
import junit.framework.TestCase;

import java.util.function.Supplier;

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

    public static <T> T runLifecycle(LifecycleComponent component, Supplier<T> body)
            throws Exception
    {
        component.construct();
        try {
            component.start();
            T result = body.get();
            component.stop();
            return result;
        }
        finally {
            component.destroy();
        }
    }

    public static void runLifecycle(LifecycleComponent component, Runnable runnable)
            throws Exception
    {
        runLifecycle(component, () -> {
            runnable.run();
            return null;
        });
    }

    public void testLifecycle()
            throws Throwable
    {
        LifecycleManager lr = new LifecycleManager();
        A a = new A();
        B b = new B();

        lr.add(b, ImmutableList.of(a));

        runLifecycle(lr, () -> {
            assertEquals(LifecycleState.STARTED, lr.getState());
            assertEquals(LifecycleState.STARTED, b.getLifecycleState());
            assertEquals(LifecycleState.STARTED, lr.getState(a));
        });
    }
}
