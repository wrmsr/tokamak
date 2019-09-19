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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.func.ToIntIntBifunction;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class MoreOptionals
{
    private MoreOptionals()
    {
    }

    public static <T> Optional<T> optionalSingle(Iterator<T> iterator)
    {
        if (iterator.hasNext()) {
            return Optional.of(checkSingle(iterator));
        }
        else {
            return Optional.empty();
        }
    }

    public static <T> Optional<T> optionalSingle(Iterable<T> iterable)
    {
        return optionalSingle(iterable.iterator());
    }

    public static OptionalInt mapOptional(OptionalInt value, IntFunction<Integer> fn)
    {
        if (value.isPresent()) {
            Integer ret = fn.apply(value.getAsInt());
            return ret == null ? OptionalInt.empty() : OptionalInt.of(ret);
        }
        else {
            return OptionalInt.empty();
        }
    }

    public static OptionalInt reduceOptionals(int identity, ToIntIntBifunction accumulator, Iterator<OptionalInt> values)
    {
        int result = identity;
        while (values.hasNext()) {
            OptionalInt value = values.next();
            if (!value.isPresent()) {
                return OptionalInt.empty();
            }
            result = accumulator.apply(identity, value.getAsInt());
        }
        return OptionalInt.of(result);
    }

    public static OptionalInt sumOptionals(Iterator<OptionalInt> values)
    {
        return reduceOptionals(0, Integer::sum, values);
    }

    public static <T> List<T> optionalToList(Optional<T> o)
    {
        if (o.isPresent()) {
            return ImmutableList.of(o.get());
        }
        else {
            return ImmutableList.of();
        }
    }

    public static <T> Set<T> optionalToSet(Optional<T> o)
    {
        if (o.isPresent()) {
            return ImmutableSet.of(o.get());
        }
        else {
            return ImmutableSet.of();
        }
    }

    public static <T> Stream<T> optionalToStream(Optional<T> o)
    {
        if (o.isPresent()) {
            return Stream.of(o.get());
        }
        else {
            return Stream.empty();
        }
    }

    public static IntStream optionalToStream(OptionalInt o)
    {
        if (o.isPresent()) {
            return IntStream.of(o.getAsInt());
        }
        else {
            return IntStream.empty();
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Optional<T> tryCast(Object obj, Class<? extends T> cls)
    {
        if (cls.isInstance(obj)) {
            return Optional.of((T) obj);
        }
        else {
            return Optional.empty();
        }
    }

    public static <T> void ifPresentOrElse(Optional<T> optional, Consumer<T> ifPresent, Runnable orElse)
    {
        if (optional.isPresent()) {
            ifPresent.accept(optional.get());
        }
        else {
            orElse.run();
        }
    }

    public static <T, R> R ifPresentOrElse(Optional<T> optional, Function<T, R> ifPresent, Supplier<R> orElse)
    {
        if (optional.isPresent()) {
            return ifPresent.apply(optional.get());
        }
        else {
            return orElse.get();
        }
    }

    public static <T> void ifNotPresent(Optional<T> optional, Runnable fn)
    {
        if (!optional.isPresent()) {
            fn.run();
        }
    }
}
