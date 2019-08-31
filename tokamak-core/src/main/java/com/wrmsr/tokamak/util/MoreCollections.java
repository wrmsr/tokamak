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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.util.collect.Ordered;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Sets.newIdentityHashSet;
import static java.util.Objects.requireNonNull;

public final class MoreCollections
{
    private MoreCollections()
    {
    }

    public static <T> T[] concatArrays(T[] a, T[] b)
    {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static <T> T concatArrays(T a, T b)
    {
        checkArgument(!a.getClass().isArray() || !b.getClass().isArray());

        Class<?> resCompType;
        Class<?> aCompType = a.getClass().getComponentType();
        Class<?> bCompType = b.getClass().getComponentType();

        if (aCompType.isAssignableFrom(bCompType)) {
            resCompType = aCompType;
        }
        else if (bCompType.isAssignableFrom(aCompType)) {
            resCompType = bCompType;
        }
        else {
            throw new IllegalArgumentException();
        }

        int aLen = Array.getLength(a);
        int bLen = Array.getLength(b);

        @SuppressWarnings("unchecked")
        T result = (T) Array.newInstance(resCompType, aLen + bLen);
        System.arraycopy(a, 0, result, 0, aLen);
        System.arraycopy(b, 0, result, aLen, bLen);

        return result;
    }

    public static <T> T[] arrayWithReplaced(T[] a, int idx, T obj)
    {
        T[] b = a.clone();
        b[idx] = obj;
        return b;
    }

    public static <T> Set<T> newIdentityHashSetOf(Iterable<T> src)
    {
        Set<T> set = newIdentityHashSet();
        for (T item : src) {
            set.add(item);
        }
        return set;
    }

    public static <T> List<T> listOf(int size, T value)
    {
        return IntStream.range(0, size).boxed().map(i -> value).collect(toImmutableList());
    }

    public static <T> Set<Set<T>> unify(Collection<Set<T>> sets)
    {
        List<Set<T>> rem = new ArrayList<>(sets);
        Set<Set<T>> ret = new HashSet<>();
        while (!rem.isEmpty()) {
            Set<T> cur = rem.remove(rem.size() - 1);
            boolean moved;
            do {
                moved = false;
                for (int i = rem.size() - 1; i >= 0; --i) {
                    if (!Sets.intersection(cur, rem.get(i)).isEmpty()) {
                        cur.addAll(rem.remove(i));
                        moved = true;
                    }
                }
            }
            while (moved);
            ret.add(cur);
        }
        return ret;
    }

    public static final class EnumeratedElement<T>
            implements Comparable<EnumeratedElement<T>>
    {
        private final int index;
        private final T item;

        public EnumeratedElement(int index, T item)
        {
            this.index = index;
            this.item = requireNonNull(item);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            EnumeratedElement<?> that = (EnumeratedElement<?>) o;
            return index == that.index &&
                    Objects.equals(item, that.item);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(index, item);
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("index", index)
                    .add("item", item)
                    .toString();
        }

        public int getIndex()
        {
            return index;
        }

        public T getItem()
        {
            return item;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public int compareTo(EnumeratedElement<T> o)
        {
            int ret = Integer.compare(index, o.index);
            if (ret != 0) {
                return ret;
            }
            return ((Comparable) item).compareTo(o.item);
        }
    }

    public static <T> Iterator<EnumeratedElement<T>> enumerate(Iterator<T> iterator)
    {
        return new Iterator<EnumeratedElement<T>>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public EnumeratedElement<T> next()
            {
                return new EnumeratedElement<>(index++, iterator.next());
            }
        };
    }

    public static <T> Iterable<EnumeratedElement<T>> enumerate(Iterable<T> iterable)
    {
        return new Iterable<EnumeratedElement<T>>()
        {
            @Override
            public Iterator<EnumeratedElement<T>> iterator()
            {
                return enumerate(iterable.iterator());
            }
        };
    }

    public static <T> Spliterator<EnumeratedElement<T>> enumerate(Spliterator<T> spliterator)
    {
        int characteristics = spliterator.characteristics() | Spliterator.NONNULL & ~Spliterator.CONCURRENT;
        return Spliterators.spliterator(enumerate(Spliterators.iterator(spliterator)), spliterator.estimateSize(), characteristics);
    }

    public static <T> Stream<EnumeratedElement<T>> enumerate(Stream<T> stream)
    {
        return StreamSupport.stream(enumerate(stream.spliterator()), false);
    }

    public static <T extends Comparable<S>, S> int compareIterators(Iterator<T> a, Iterator<S> b)
    {
        while (a.hasNext() && b.hasNext()) {
            int comparison = a.next().compareTo(b.next());
            if (comparison != 0) {
                return comparison;
            }
        }
        if (a.hasNext()) {
            return 1;
        }
        if (b.hasNext()) {
            return -1;
        }
        return 0;
    }

    public static <T extends Set<?>> boolean isOrdered(T obj)
    {
        return obj instanceof ImmutableSet || obj instanceof LinkedHashSet || obj instanceof Ordered;
    }

    public static <T extends Set<?>> T checkOrdered(T obj)
    {
        checkState(isOrdered(obj));
        return obj;
    }

    public static <T extends Map<?, ?>> boolean isOrdered(T obj)
    {
        return obj instanceof ImmutableMap || obj instanceof LinkedHashMap || obj instanceof Ordered;
    }

    public static <T extends Map<?, ?>> T checkOrdered(T obj)
    {
        checkState(isOrdered(obj));
        return obj;
    }

    public static <T extends Collection<?>> boolean isOrdered(T obj)
    {
        return obj instanceof List ||
                (obj instanceof Map && isOrdered((Map) obj)) ||
                (obj instanceof Set && isOrdered((Set) obj));
    }

    public static <T extends Collection<?>> T checkOrdered(T obj)
    {
        checkState(isOrdered(obj));
        return obj;
    }

    public static <T extends Iterable<?>> boolean isOrdered(T obj)
    {
        return (obj instanceof Collection && isOrdered((Collection) obj));
    }

    public static <T extends Iterable<?>> T checkOrdered(T obj)
    {
        checkState(isOrdered(obj));
        return obj;
    }
}
