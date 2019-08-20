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
import com.google.common.collect.Streams;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;
import static java.util.function.Function.identity;

public final class ObjectArrayBackedMap<K>
        implements Map<K, Object>
{
    @Immutable
    public static final class Shape<K>
            implements StreamableIterable<K>
    {
        private final List<K> keys;
        private final Map<K, Integer> indicesByKey;

        public Shape(Iterable<K> keys)
        {
            this.keys = checkUnique(ImmutableList.copyOf(keys));
            indicesByKey = IntStream.range(0, this.keys.size()).boxed().collect(toImmutableMap(this.keys::get, identity()));
        }

        public List<K> getKeys()
        {
            return keys;
        }

        public Map<K, Integer> getIndicesByKey()
        {
            return indicesByKey;
        }

        public int size()
        {
            return keys.size();
        }

        @Override
        public Iterator<K> iterator()
        {
            return keys.iterator();
        }
    }

    private final Shape<K> shape;
    private final Object[] values;

    public ObjectArrayBackedMap(Shape<K> shape)
    {
        this.shape = checkNotNull(shape);
        values = new Object[shape.size()];
    }

    public ObjectArrayBackedMap(Iterable<K> keys)
    {
        this(new Shape<>(keys));
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
        return Streams.zip(shape.stream(), Arrays.stream(values), Pair::immutable).collect(toImmutableSet());
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
        return shape.indicesByKey.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        for (int i = 0; i < values.length; ++i) {
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
        return shape.indicesByKey.keySet();
    }

    @Override
    public Collection<Object> values()
    {
        return new ArrayListView<>(values);
    }
}
