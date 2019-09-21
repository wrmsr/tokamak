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

import com.wrmsr.tokamak.util.func.ThrowingConsumer;
import com.wrmsr.tokamak.util.func.ThrowingFunction;
import com.wrmsr.tokamak.util.func.ThrowingRunnable;

public final class Lifecycles
{
    /*
    TODO:
     - self-deps (including lifecycle depping registry itself)
     - injector integration
     - @PostConstruct / javax.inject interop?
     - LifecycleController parent/hierarchy, ensure 0 or 1 parent
     - @WeakLifecycleDependency - circdeps
    */

    private Lifecycles()
    {
    }

    public static <T extends Lifecycle, R> R applyLifecycle(T lifecycle, ThrowingFunction<T, R> body)
            throws Exception
    {
        lifecycle.construct();
        try {
            lifecycle.start();
            R result = body.apply(lifecycle);
            lifecycle.stop();
            return result;
        }
        finally {
            lifecycle.destroy();
        }
    }

    public static <T extends Lifecycle> void runLifecycle(T lifecycle, ThrowingConsumer<T> consumer)
            throws Exception
    {
        applyLifecycle(lifecycle, (c) -> {
            consumer.accept(c);
            return null;
        });
    }

    public static void runLifecycle(Lifecycle lifecycle, ThrowingRunnable runnable)
            throws Exception
    {
        applyLifecycle(lifecycle, (c) -> {
            runnable.run();
            return null;
        });
    }
}
