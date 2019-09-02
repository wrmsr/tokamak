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
package com.wrmsr.tokamak.util.kv;

import com.wrmsr.tokamak.util.codec.Codec;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Kv<K, V>
        extends Iterable<K>
{
    V get(K key);

    default boolean containsKey(K key)
    {
        return get(key) != null;
    }

    void put(K key, V value);

    void remove(K key);

    @Override
    default Iterator<K> iterator()
    {
        throw new UnsupportedOperationException();
    }

    interface ManagedIterator<K>
            extends Iterator<K>, AutoCloseable
    {
        @Override
        default void close()
        {
        }

        static <K> ManagedIterator<K> wrap(Iterator<K> target)
        {
            return new ManagedIterator<K>()
            {
                @Override
                public boolean hasNext()
                {
                    return target.hasNext();
                }

                @Override
                public K next()
                {
                    return target.next();
                }
            };
        }
    }

    default ManagedIterator<K> managedIterator()
    {
        return ManagedIterator.wrap(iterator());
    }

    class FromMap<K, V>
            implements Kv<K, V>
    {
        private final Map<K, V> target;

        public FromMap(Map<K, V> target)
        {
            this.target = target;
        }

        @Override
        public V get(K key)
        {
            return target.get(key);
        }

        @Override
        public boolean containsKey(K key)
        {
            return target.containsKey(key);
        }

        @Override
        public void put(K key, V value)
        {
            target.put(key, value);
        }

        @Override
        public void remove(K key)
        {
            target.remove(key);
        }

        @Override
        public Iterator<K> iterator()
        {
            return target.keySet().iterator();
        }
    }

    abstract class AbstractToMap<K, V>
            implements Map<K, V>
    {
        private final Kv<K, V> target;
        private final boolean keyCastsThrow;
        private final boolean getForReturns;

        public AbstractToMap(Kv<K, V> target, boolean keyCastsThrow, boolean getForReturns)
        {
            this.target = target;
            this.keyCastsThrow = keyCastsThrow;
            this.getForReturns = getForReturns;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public V remove(Object key)
        {
            final K k;
            try {
                k = (K) key;
            }
            catch (ClassCastException e) {
                if (keyCastsThrow) {
                    throw e;
                }
                return null;
            }
            final V ret = getForReturns ? target.get(k) : null;
            target.remove(k);
            return ret;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public V get(Object key)
        {
            final K k;
            try {
                k = (K) key;
            }
            catch (ClassCastException e) {
                if (keyCastsThrow) {
                    throw e;
                }
                return null;
            }
            return target.get(k);
        }

        @Override
        public V put(K key, V value)
        {
            final V ret = getForReturns ? target.get(key) : null;
            target.put(key, value);
            return ret;
        }

        @Override
        public boolean containsKey(Object key)
        {
            final K k;
            try {
                k = (K) key;
            }
            catch (ClassCastException e) {
                if (keyCastsThrow) {
                    throw e;
                }
                return false;
            }
            return target.containsKey(k);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m)
        {
            m.entrySet().stream().forEach(e -> put(e.getKey(), e.getValue()));
        }
    }

    class RestrictedToMap<K, V>
            extends AbstractToMap<K, V>
    {
        public RestrictedToMap(Kv<K, V> target, boolean keyCastsThrow, boolean getForReturns)
        {
            super(target, keyCastsThrow, getForReturns);
        }

        @Override
        public int size()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<K> keySet()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<V> values()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<K, V>> entrySet()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public V getOrDefault(Object key, V defaultValue)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(K key, V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K key, V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
        {
            throw new UnsupportedOperationException();
        }
    }

    class UnrestrictedToMap<K, V>
            extends AbstractToMap<K, V>
    {
        public UnrestrictedToMap(Kv<K, V> target, boolean keyCastsThrow, boolean getForReturns)
        {
            super(target, keyCastsThrow, getForReturns);
        }

        @Override
        public int size()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<K> keySet()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<V> values()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<K, V>> entrySet()
        {
            throw new UnsupportedOperationException();
        }
    }

    class KeyCodec<KO, KI, V>
            implements Kv<KO, V>
    {
        protected final Kv<KI, V> wrapped;
        protected final Codec<KO, KI> codec;

        public KeyCodec(Kv<KI, V> wrapped, Codec<KO, KI> codec)
        {
            this.wrapped = wrapped;
            this.codec = codec;
        }

        @Override
        public V get(KO key)
        {
            return wrapped.get(codec.encode(key));
        }

        @Override
        public void put(KO key, V value)
        {
            wrapped.put(codec.encode(key), value);
        }

        @Override
        public void remove(KO key)
        {
            wrapped.remove(codec.encode(key));
        }

        @Override
        public boolean containsKey(KO key)
        {
            return wrapped.containsKey(codec.encode(key));
        }

        @Override
        public Iterator<KO> iterator()
        {
            Iterator<KI> it = wrapped.iterator();
            return new Iterator<KO>()
            {
                @Override
                public boolean hasNext()
                {
                    return it.hasNext();
                }

                @Override
                public KO next()
                {
                    return codec.decode(it.next());
                }
            };
        }

        @Override
        public ManagedIterator<KO> managedIterator()
        {
            ManagedIterator<KI> it = wrapped.managedIterator();
            return new ManagedIterator<KO>()
            {
                @Override
                public void close()
                {
                    it.close();
                }

                @Override
                public boolean hasNext()
                {
                    return it.hasNext();
                }

                @Override
                public KO next()
                {
                    return codec.decode(it.next());
                }
            };
        }
    }

    class ValueCodec<K, VO, VI>
            implements Kv<K, VO>
    {
        protected final Kv<K, VI> wrapped;
        protected final Codec<VO, VI> codec;

        public ValueCodec(Kv<K, VI> wrapped, Codec<VO, VI> codec)
        {
            this.wrapped = wrapped;
            this.codec = codec;
        }

        @Override
        public VO get(K key)
        {
            VI v = wrapped.get(key);
            if (v != null) {
                return codec.decode(v);
            }
            else {
                return null;
            }
        }

        @Override
        public boolean containsKey(K key)
        {
            return wrapped.containsKey(key);
        }

        @Override
        public void put(K key, VO value)
        {
            wrapped.put(key, codec.encode(value));
        }

        @Override
        public void remove(K key)
        {
            wrapped.remove(key);
        }

        @Override
        public Iterator<K> iterator()
        {
            return wrapped.iterator();
        }

        @Override
        public ManagedIterator<K> managedIterator()
        {
            return wrapped.managedIterator();
        }
    }
}
