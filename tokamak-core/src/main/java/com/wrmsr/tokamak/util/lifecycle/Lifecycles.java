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

import com.wrmsr.tokamak.util.func.ThrowingFunction;
import com.wrmsr.tokamak.util.func.ThrowingRunnable;

public final class Lifecycles
{
    /*
    TODO:
     - self-deps (including component depping registry itself)
     - injector integration
     - @PostConstruct / javax.inject interop?
     - LifecycleController parent/hierarchy, ensure 0 or 1 parent
    */

    private Lifecycles()
    {
    }

    public static <T extends LifecycleComponent, R> R applyLifecycle(T component, ThrowingFunction<T, R> body)
            throws Exception
    {
        component.construct();
        try {
            component.start();
            R result = body.apply(component);
            component.stop();
            return result;
        }
        finally {
            component.destroy();
        }
    }

    public static void runLifecycle(LifecycleComponent component, ThrowingRunnable runnable)
            throws Exception
    {
        applyLifecycle(component, (c) -> {
            runnable.run();
            return null;
        });
    }
}
