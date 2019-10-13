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
package com.wrmsr.tokamak.util.func;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableThrowingConsumer<T>
{
    void accept(T t)
            throws Throwable;

    static <T> void throwableRethrowingAccept(ThrowableThrowingConsumer<T> consumer, T t)
    {
        try {
            consumer.accept(t);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        catch (Throwable e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            throw new RuntimeException(e);
        }
    }

    static <T> Consumer<T> throwableRethrowing(ThrowableThrowingConsumer<T> consumer)
    {
        return (t) -> throwableRethrowingAccept(consumer, t);
    }
}
