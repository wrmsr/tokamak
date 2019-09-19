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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

@Immutable
public final class Span<T>
{
    public enum Bound
    {
        BELOW,
        EXACTLY,
        ABOVE
    }

    @Immutable
    public static final class Marker<T>
    {
        private final T value;
        private final Bound bound;

        @JsonCreator
        public Marker(
                @JsonProperty("value") T value,
                @JsonProperty("bound") Bound bound)
        {
            this.value = value;
            this.bound = bound;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Marker<?> marker = (Marker<?>) o;
            return Objects.equals(value, marker.value) &&
                    bound == marker.bound;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(value, bound);
        }

        @Override
        public String toString()
        {
            return "Marker{" +
                    "value=" + value +
                    ", bound=" + bound +
                    '}';
        }

        @JsonProperty("value")
        public T getValue()
        {
            return value;
        }

        @JsonProperty("bound")
        public Bound getBound()
        {
            return bound;
        }
    }

    private final Marker<T> lower;
    private final Marker<T> upper;

    @JsonCreator
    public Span(
            @JsonProperty("lower") Marker<T> lower,
            @JsonProperty("upper") Marker<T> upper)
    {
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Span<?> span = (Span<?>) o;
        return Objects.equals(lower, span.lower) &&
                Objects.equals(upper, span.upper);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lower, upper);
    }

    @Override
    public String toString()
    {
        return "Span{" +
                "lower=" + lower +
                ", upper=" + upper +
                '}';
    }

    @JsonProperty("lower")
    public Marker<T> getLower()
    {
        return lower;
    }

    @JsonProperty("upper")
    public Marker<T> getUpper()
    {
        return upper;
    }
}
