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
package com.wrmsr.tokamak.util.match;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Match<T>
{
    public abstract boolean isPresent();

    public abstract T getValue();

    public abstract Captures getCaptures();

    public abstract Match<T> filter(Predicate<? super T> predicate);

    public abstract <U> Match<U> map(Function<? super T, ? extends U> mapper);

    public abstract <U> Match<U> flatMap(Function<? super T, Match<U>> mapper);

    public static <T> Match<T> of(T value, Captures captures)
    {
        return new Present<>(value, captures);
    }

    public static <T> Match<T> empty()
    {
        return new Empty<>();
    }

    private static final class Present<T>
            extends Match<T>
    {
        private final T value;
        private final Captures captures;

        public Present(T value, Captures captures)
        {
            this.value = checkNotNull(value);
            this.captures = checkNotNull(captures);
        }

        @Override
        public boolean isPresent()
        {
            return true;
        }

        @Override
        public T getValue()
        {
            return value;
        }

        @Override
        public Captures getCaptures()
        {
            return captures;
        }

        @Override
        public Match<T> filter(Predicate<? super T> predicate)
        {
            return predicate.test(value) ? this : empty();
        }

        @Override
        public <U> Match<U> map(Function<? super T, ? extends U> mapper)
        {
            return of(mapper.apply(value), captures);
        }

        @Override
        public <U> Match<U> flatMap(Function<? super T, Match<U>> mapper)
        {
            return mapper.apply(value);
        }
    }

    private static final class Empty<T>
            extends Match<T>
    {
        @Override
        public boolean isPresent()
        {
            return false;
        }

        @Override
        public T getValue()
        {
            throw new IllegalStateException();
        }

        @Override
        public Captures getCaptures()
        {
            throw new IllegalStateException();
        }

        @Override
        public Match<T> filter(Predicate<? super T> predicate)
        {
            return this;
        }

        @Override
        public <U> Match<U> map(Function<? super T, ? extends U> mapper)
        {
            return empty();
        }

        @Override
        public <U> Match<U> flatMap(Function<? super T, Match<U>> mapper)
        {
            return empty();
        }
    }
}
