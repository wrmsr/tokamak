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

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Cell<T>
{
    /*
    TODO:
     - actual optional
     - synchronized
    */

    T get();

    void set(T value);

    default Supplier<T> toSupplier()
    {
        return this::get;
    }

    default Consumer<T> toConsumer()
    {
        return this::set;
    }

    final class DefaultImpl<T>
            implements Cell<T>
    {
        private T value;

        public DefaultImpl(T value)
        {
            this.value = value;
        }

        @Override
        public final T get()
        {
            return value;
        }

        @Override
        public final void set(T value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "{value=" + value + '}';
        }
    }

    static <T> Cell<T> of(T value)
    {
        return new DefaultImpl<>(value);
    }

    final class OptionalImpl<T>
            implements Cell<T>
    {
        private boolean isSet;
        private T value;

        @Override
        public T get()
        {
            if (!isSet) {
                throw new IllegalStateException();
            }
            return value;
        }

        @Override
        public void set(T value)
        {
            isSet = true;
            this.value = value;
        }
    }

    static <T> Cell<T> optional()
    {
        return new OptionalImpl<>();
    }

    final class SetOnceImpl<T>
            implements Cell<T>
    {
        private boolean isSet;
        private T value;

        @Override
        public T get()
        {
            if (!isSet) {
                throw new IllegalStateException();
            }
            return value;
        }

        @Override
        public void set(T value)
        {
            if (isSet) {
                throw new IllegalStateException();
            }
            isSet = true;
            this.value = value;
        }
    }

    static <T> Cell<T> setOnce()
    {
        return new SetOnceImpl<>();
    }
}
