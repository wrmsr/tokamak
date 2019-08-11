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

import java.util.function.Function;
import java.util.function.Supplier;

public final class MoreExceptions
{
    private MoreExceptions()
    {
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R>
    {
        R apply(T t)
                throws Exception;
    }

    public static <T, R> Function<T, R> runtimeThrowing(ThrowingFunction<T, R> fn)
    {
        return t -> {
            try {
                return fn.apply(t);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    public interface ThrowingSupplier<R>
    {
        R get()
                throws Exception;
    }

    public static <R> Supplier<R> runtimeThrowing(ThrowingSupplier<R> fn)
    {
        return () -> {
            try {
                return fn.get();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    public interface ThrowingRunnable
    {
        void run()
                throws Exception;
    }

    public static Runnable runtimeThrowing(ThrowingRunnable fn)
    {
        return () -> {
            try {
                fn.run();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
