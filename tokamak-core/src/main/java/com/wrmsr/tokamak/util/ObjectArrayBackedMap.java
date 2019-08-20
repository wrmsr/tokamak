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
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;
import static java.util.function.Function.identity;

public final class ObjectArrayBackedMap<K>
        implements Map<K, Object>
{
    @Immutable
    public static final class Shape<K>
            implements Set<K>
    {
        private final Map<K, Integer> indicesByKey;
        private final int width;

        public Shape(Map<K, Integer> indicesByKey, int width)
        {
            this.indicesByKey = ImmutableMap.copyOf(indicesByKey);
            this.width = width;
            checkArgument(width >= 0);
            this.indicesByKey.values().forEach(i -> checkArgument(i >= 0 && i < width));
        }

        public Shape(Iterable<K> keys)
        {
            List<K> keyList = checkUnique(ImmutableList.copyOf(keys));
            indicesByKey = IntStream.range(0, keyList.size()).boxed().collect(toImmutableMap(keyList::get, identity()));
            width = indicesByKey.size();
        }

        public static <K> Shape<K> of(Iterable<K> keys)
        {
            checkNotNull(keys);
            return keys instanceof Shape ? (Shape<K>) keys : new Shape<>(keys);
        }

        public Set<K> getKeys()
        {
            return indicesByKey.keySet();
        }

        public Map<K, Integer> getIndicesByKey()
        {
            return indicesByKey;
        }

        public int getWidth()
        {
            return width;
        }

        public int size()
        {
            return indicesByKey.size();
        }

        @Override
        public Iterator<K> iterator()
        {
            return indicesByKey.keySet().iterator();
        }

        @Override
        public boolean isEmpty()
        {
            return indicesByKey.isEmpty();
        }

        @Override
        public boolean contains(Object o)
        {
            return indicesByKey.containsKey((K) o);
        }

        @Override
        public Object[] toArray()
        {
            return indicesByKey.keySet().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a)
        {
            return indicesByKey.keySet().toArray(a);
        }

        @Override
        public boolean add(K k)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            return indicesByKey.keySet().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends K> c)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Spliterator<K> spliterator()
        {
            return indicesByKey.keySet().spliterator();
        }

        @Override
        public Stream<K> stream()
        {
            return indicesByKey.keySet().stream();
        }
    }

    private final Shape<K> shape;
    private final Object[] values;

    public ObjectArrayBackedMap(Shape<K> shape, Object[] values)
    {
        checkArgument(values.length == shape.width);
        this.shape = checkNotNull(shape);
        this.values = checkNotNull(values);
    }

    public ObjectArrayBackedMap(Iterable<K> keys)
    {
        this.shape = Shape.of(keys);
        values = new Object[shape.getWidth()];
    }

    public Shape<K> getShape()
    {
        return shape;
    }

    public Object[] getValuesArray()
    {
        return values;
    }

    @Override
    public int size()
    {
        return values.length;
    }

    @Override
    public Set<Entry<K, Object>> entrySet()
    {
        return shape.indicesByKey.entrySet().stream()
                .map(e -> Pair.immutable(e.getKey(), values[e.getValue()]))
                .collect(toImmutableSet());
    }

    @Override
    public Object put(K key, Object value)
    {
        int idx = shape.indicesByKey.get(key);
        Object ret = values[idx];
        values[idx] = value;
        return ret;
    }

    public Object get(int idx)
    {
        return values[idx];
    }

    @Override
    public boolean isEmpty()
    {
        return shape.size() == 0;
    }

    @Override
    public boolean containsKey(Object key)
    {
        return shape.contains(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        for (int i : shape.indicesByKey.values()) {
            if (Objects.equals(value, values[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key)
    {
        return values[shape.indicesByKey.get(key)];
    }

    @Override
    public Object remove(Object key)
    {
        return put((K) key, null);
    }

    @Override
    public void putAll(Map<? extends K, ?> m)
    {
        for (Map.Entry e : m.entrySet()) {
            put((K) e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear()
    {
        Arrays.fill(values, null);
    }

    @Override
    public Set<K> keySet()
    {
        return shape;
    }

    @Override
    public Collection<Object> values()
    {
        return shape.indicesByKey.values().stream()
                .map(i -> values[i])
                .collect(toImmutableList());
    }

    public static final class Builder<K>
    {
        private final ObjectArrayBackedMap<K> map;
        private final BitSet bitSet;

        private Builder(Shape<K> shape)
        {
            map = new ObjectArrayBackedMap<>(shape);
            bitSet = new BitSet(map.size());
        }

        public Builder<K> put(K key, Object value)
        {
            int idx = map.shape.indicesByKey.get(key);
            checkState(!bitSet.get(idx));
            map.values[idx] = value;
            bitSet.set(idx);
            return this;
        }

        public Builder<K> putAll(Map<? extends K, ?> map)
        {
            for (Map.Entry e : map.entrySet()) {
                put((K) e.getKey(), e.getValue());
            }
            return this;
        }

        public ObjectArrayBackedMap<K> build()
        {
            return map;
        }
    }

    public static <K> Builder<K> builder(Iterable<K> keys)
    {
        return new Builder<>(Shape.of(keys));
    }
}
