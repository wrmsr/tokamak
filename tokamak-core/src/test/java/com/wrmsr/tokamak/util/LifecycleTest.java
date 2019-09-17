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
import com.wrmsr.tokamak.util.lifecycle.LifecycleRegistry;
import org.junit.Test;

public class LifecycleTest
{
    public static class A
            implements LifecycleComponent
    {

    }

    public static class B
            extends AbstractLifecycleComponent
    {

    }

    @Test
    public void testLifecycle()
            throws Throwable
    {
        LifecycleRegistry lr = new LifecycleRegistry();
        A a = new A();
        B b = new B();

        lr.postConstruct();

        // lr.add(a);
        lr.add(b, ImmutableList.of(a));

        lr.start();

        lr.stop();

        lr.close();
    }
}
