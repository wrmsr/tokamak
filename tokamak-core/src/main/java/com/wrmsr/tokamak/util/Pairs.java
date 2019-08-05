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

import java.util.Map;
import java.util.Objects;

public final class Pairs
{
    private Pairs()
    {
    }

    @javax.annotation.concurrent.Immutable
    public static final class Immutable<K, V>
            implements Map.Entry<K, V>
    {
        private final K key;
        private final V value;

        @JsonCreator
        public Immutable(
                @JsonProperty("key") K key,
                @JsonProperty("value") V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Immutable<?, ?> pair = (Immutable<?, ?>) o;
            return Objects.equals(key, pair.key) &&
                    Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(key, value);
        }

        @Override
        public String toString()
        {
            return "Pair{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }

        @Override
        @JsonProperty("key")
        public K getKey()
        {
            return key;
        }

        @Override
        @JsonProperty("value")
        public V getValue()
        {
            return value;
        }

        @Override
        public V setValue(V value)
        {
            throw new UnsupportedOperationException();
        }
    }

    public static final class Mutable<K, V>
            implements Map.Entry<K, V>
    {
        private K key;
        private V value;

        @JsonCreator
        public Mutable(
                @JsonProperty("key") K key,
                @JsonProperty("value") V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Mutable<?, ?> pair = (Mutable<?, ?>) o;
            return Objects.equals(key, pair.key) &&
                    Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(key, value);
        }

        @Override
        public String toString()
        {
            return "Pair{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }

        @Override
        @JsonProperty("key")
        public K getKey()
        {
            return key;
        }

        @Override
        @JsonProperty("value")
        public V getValue()
        {
            return value;
        }

        @Override
        public V setValue(V value)
        {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
}
