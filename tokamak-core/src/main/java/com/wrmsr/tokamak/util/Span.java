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

import javax.annotation.concurrent.Immutable;

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

        public Marker(T value, Bound bound)
        {
            this.value = value;
            this.bound = bound;
        }

        public T getValue()
        {
            return value;
        }

        public Bound getBound()
        {
            return bound;
        }
    }

    private final Marker<T> lower;
    private final Marker<T> upper;

    public Span(Marker<T> lower, Marker<T> upper)
    {
        this.lower = lower;
        this.upper = upper;
    }

    public Marker<T> getLower()
    {
        return lower;
    }

    public Marker<T> getUpper()
    {
        return upper;
    }
}
