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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;

public final class MoreFunctions
{
    private MoreFunctions()
    {
    }

    public static Runnable nop()
    {
        return () -> {
        };
    }

    public static <R> Supplier<R> konst(R ret)
    {
        return () -> ret;
    }

    public static <T, R> Function<T, R> konst(Supplier<R> ret)
    {
        return arg0 -> ret.get();
    }

    public static <T, U, R> BiFunction<T, U, R> konst(Function<T, R> ret)
    {
        return (arg0, arg1) -> ret.apply(arg0);
    }

    public static <T> Predicate<T> negate(Predicate<T> pred)
    {
        return pred.negate();
    }

    public static <T> Runnable discard(Supplier<T> supplier)
    {
        return supplier::get;
    }

    public static <T> Supplier<T> supply(Runnable runnable, T value)
    {
        return () -> {
            runnable.run();
            return value;
        };
    }

    public static <T> Supplier<T> supplyNull(Runnable runnable)
    {
        return supply(runnable, null);
    }

    public static <T> Supplier<T> once(Supplier<T> supplier)
    {
        Cell<Boolean> flag = Cell.of(false);
        return () -> {
            checkState(!flag.get());
            T value = supplier.get();
            flag.set(true);
            return value;
        };
    }

    public static <T> Consumer<T> once(Consumer<T> consumer)
    {
        Cell<Boolean> flag = Cell.of(false);
        return (value) -> {
            checkState(!flag.get());
            consumer.accept(value);
            flag.set(true);
        };
    }

    public static Runnable once(Runnable runnable)
    {
        Cell<Boolean> flag = Cell.of(false);
        return () -> {
            checkState(!flag.get());
            runnable.run();
            flag.set(true);
        };
    }

    public static <T> Supplier<T> memoize(Supplier<T> supplier)
    {
        Cell<T> value = Cell.of(null);
        Cell<Boolean> flag = Cell.of(false);
        return () -> {
            if (!flag.get()) {
                value.set(supplier.get());
                flag.set(true);
            }
            return value.get();
        };
    }

    public static <T> Consumer<T> memoize(Consumer<T> consumer)
    {
        Cell<Boolean> flag = Cell.of(false);
        return (value) -> {
            if (!flag.get()) {
                consumer.accept(value);
                flag.set(true);
            }
        };
    }

    public static Runnable memoize(Runnable runnable)
    {
        Cell<Boolean> flag = Cell.of(false);
        return () -> {
            if (!flag.get()) {
                runnable.run();
                flag.set(true);
            }
        };
    }
}
