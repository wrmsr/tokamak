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
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.wrmsr.tokamak.util.collect.Ordered;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newIdentityHashSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

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

    public static <K, V> Map<V, K> invertMap(Map<K, V> map)
    {
        return map.entrySet().stream().collect(toImmutableMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public static <K, V> Map<V, Set<K>> invertSetMap(Map<K, Set<V>> map)
    {
        Map<V, Set<K>> ret = new LinkedHashMap<>();
        map.forEach((k, vs) -> {
            vs.forEach(v -> {
                ret.computeIfAbsent(v, v_ -> new LinkedHashSet<>()).add(k);
            });
        });
        return newImmutableSetMap(ret);
    }

    public static <T> Set<T> newIdentityHashSetOf(Iterable<T> src)
    {
        Set<T> set = newIdentityHashSet();
        for (T item : src) {
            set.add(item);
        }
        return set;
    }

    public static <K, V> V safeComputeIfAbsent(Map<K, V> map, K key, Function<K, V> fn)
    {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        V value = fn.apply(key);
        if (map.containsKey(key)) {
            checkState(map.get(key).equals(value));
        }
        else {
            map.put(key, value);
        }
        return value;
    }

    public static <T0, T1> List<T1> immutableMapItems(Iterator<T0> iterator, Function<T0, T1> fn)
    {
        return streamIterator(iterator).map(fn).collect(toImmutableList());
    }

    public static <T0, T1> List<T1> immutableMapItems(Iterable<T0> iterable, Function<T0, T1> fn)
    {
        return StreamSupport.stream(iterable.spliterator(), false).map(fn).collect(toImmutableList());
    }

    public static <K, V0, V1> Map<K, V1> immutableMapValues(Map<K, V0> map, Function<V0, V1> fn)
    {
        return map.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> fn.apply(e.getValue())));
    }

    public static <K, V> Map<K, List<V>> newImmutableListMap(Map<K, List<V>> map)
    {
        return immutableMapValues(map, ImmutableList::copyOf);
    }

    public static <K, V> Map<K, Set<V>> newImmutableSetMap(Map<K, Set<V>> map)
    {
        return immutableMapValues(map, ImmutableSet::copyOf);
    }

    public static <K0, K1, V> Map<K0, Map<K1, Set<V>>> newImmutableSetMapMap(Map<K0, Map<K1, Set<V>>> map)
    {
        return immutableMapValues(map, m -> immutableMapValues(m, ImmutableSet::copyOf));
    }

    public static <T> List<T> reversedImmutableListOf(Iterator<T> iterator)
    {
        return ImmutableList.copyOf(Lists.reverse(newArrayList(iterator)));
    }

    public static <T> List<T> reversedImmutableListOf(Iterable<T> iterable)
    {
        return reversedImmutableListOf(iterable.iterator());
    }

    public static <K, V> Map<K, V> immutableMapOfSame(Iterable<K> keys, V value)
    {
        return StreamSupport.stream(keys.spliterator(), false).collect(toImmutableMap(identity(), k -> value));
    }

    public static <T> List<T> listOf(int size, T value)
    {
        return IntStream.range(0, size).boxed().map(i -> value).collect(toImmutableList());
    }

    public static <T> List<Set<T>> unify(Iterable<Set<T>> sets)
    {
        List<Set<T>> rem = newArrayList(sets);
        List<Set<T>> ret = new ArrayList<>();
        while (!rem.isEmpty()) {
            Set<T> cur = rem.remove(rem.size() - 1);
            boolean moved;
            do {
                moved = false;
                for (int i = rem.size() - 1; i >= 0; --i) {
                    if (rem.get(i).stream().anyMatch(cur::contains)) {
                        cur.addAll(rem.remove(i));
                        moved = true;
                    }
                }
            }
            while (moved);
            ret.add(cur);
        }
        if (!ret.isEmpty()) {
            Set<T> all = ret.stream().flatMap(Set::stream).collect(toImmutableSet());
            int num = ret.stream().map(Set::size).reduce(Integer::sum).get();
            checkState(all.size() == num);
        }
        return ret;
    }

    public static final class EnumeratedElement<T>
            implements Map.Entry<Integer, T>, Comparable<EnumeratedElement<T>>
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
        public Integer getKey()
        {
            return index;
        }

        @Override
        public T getValue()
        {
            return item;
        }

        @Override
        public T setValue(T value)
        {
            throw new UnsupportedOperationException();
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

    public static <T> Map<T, Integer> buildListIndexMap(List<T> list)
    {
        return Streams.zip(
                IntStream.range(0, list.size()).boxed(), list.stream(), (i, n) -> new Pair.Immutable<>(n, i)
        ).collect(toImmutableMap());
    }

    public static <T> List<T> sorted(Iterator<T> iterator, Comparator<? super T> c)
    {
        List<T> list = newArrayList(iterator);
        list.sort(c);
        return ImmutableList.copyOf(list);
    }

    public static <T> List<T> sorted(Iterable<T> iterable, Comparator<? super T> c)
    {
        return sorted(iterable.iterator(), c);
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

    public static <T> Iterator<T> arrayIterate(T[] arr)
    {
        return new Iterator<T>()
        {
            private int idx = 0;

            @Override
            public boolean hasNext()
            {
                return idx < arr.length;
            }

            @Override
            public T next()
            {
                return arr[idx++];
            }
        };
    }

    public static <T> Stream<T> streamIterator(Iterator<T> iterator)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    public static <T> Map<T, Long> histogram(Stream<T> stream)
    {
        return stream.collect(Collectors.groupingBy(identity(), Collectors.counting()));
    }

    public static <T> Map<T, Long> histogram(Iterator<T> iterator)
    {
        return histogram(streamIterator(iterator));
    }

    public static <T> void delimitedForEach(Iterable<T> items, Runnable delimiter, Consumer<T> consumer)
    {
        boolean delimit = false;
        for (T item : items) {
            if (delimit) {
                delimiter.run();
            }
            else {
                delimit = true;
            }
            consumer.accept(item);
        }
    }
}
