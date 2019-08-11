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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;

public final class MoreCollectors
{
    private MoreCollectors()
    {
    }

    public static <T> Collector<T, ?, ImmutableMultiset<T>> toImmutableMultiset()
    {
        return Collector.<T, ImmutableMultiset.Builder<T>, ImmutableMultiset<T>>of(
                ImmutableMultiset.Builder::new,
                ImmutableMultiset.Builder::add,
                (left, right) -> {
                    left.addAll(right.build());
                    return left;
                },
                ImmutableMultiset.Builder::build,
                Collector.Characteristics.UNORDERED);
    }

    public static <I, K, V> Collector<I, ImmutableMultimap.Builder<K, V>, ImmutableMultimap<K, V>> toImmutableMultimap(
            Function<I, K> keyMapper,
            Function<I, V> valueMapper)
    {
        return Collector.of(
                ImmutableMultimap::builder,
                (builder, in) -> builder.put(keyMapper.apply(in), valueMapper.apply(in)),
                (ImmutableMultimap.Builder<K, V> left, ImmutableMultimap.Builder<K, V> right) -> left.putAll(right.build()),
                ImmutableMultimap.Builder::build);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ImmutableMultimap.Builder<K, V>, ImmutableMultimap<K, V>> toImmutableMultimap()
    {
        return toImmutableMultimap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private static <T> BinaryOperator<T> throwingMerger()
    {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toIdentityMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), IdentityHashMap::new);
    }

    public static <T> Collector<T, ?, Set<T>> toIdentitySet()
    {
        return Collector.<T, Set<T>, Set<T>>of(
                com.google.common.collect.Sets::newIdentityHashSet,
                Set::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                identity(),
                Collector.Characteristics.UNORDERED);
    }

    public static <T> Collector<T, ?, Set<T>> toHashSet()
    {
        return Collector.of(
                HashSet::new,
                Set::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                identity(),
                Collector.Characteristics.UNORDERED);
    }

    public static <I, K, V> Collector<I, Map<K, V>, Map<K, V>> toHashMap(
            Function<I, K> keyMapper,
            Function<I, V> valueMapper)
    {
        return Collector.of(
                HashMap::new,
                (map, item) -> map.put(keyMapper.apply(item), valueMapper.apply(item)),
                (Map<K, V> left, Map<K, V> right) -> {
                    left.putAll(right);
                    return left;
                },
                identity(),
                Collector.Characteristics.UNORDERED);
    }

    public static <K, V> Collector<Map.Entry<K, V>, Map<K, V>, Map<K, V>> toHashMap()
    {
        return toHashMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T> Collector<T, ?, List<T>> toArrayList()
    {
        return Collector.of(
                ArrayList::new,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                identity());
    }

    public static <I, K, V> Collector<I, SetMultimap<K, V>, SetMultimap<K, V>> toHashMultimap(
            Function<I, K> keyMapper,
            Function<I, V> valueMapper)
    {
        return Collector.of(
                HashMultimap::create,
                (SetMultimap<K, V> map, I in) -> map.put(keyMapper.apply(in), valueMapper.apply(in)),
                (SetMultimap<K, V> left, SetMultimap<K, V> right) -> {
                    left.putAll(right);
                    return left;
                },
                identity(),
                Collector.Characteristics.UNORDERED);
    }

    public static <K, V> Collector<Map.Entry<K, V>, SetMultimap<K, V>, SetMultimap<K, V>> toHashMultimap()
    {
        return toHashMultimap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static class ImmutableGroupingByCollector<T, A, K>
            implements Collector<T, A, Map<K, List<T>>>
    {
        private final Collector<T, A, Map<K, List<T>>> child;

        public ImmutableGroupingByCollector(Collector<T, A, Map<K, List<T>>> child)
        {
            this.child = child;
        }

        @Override
        public Supplier<A> supplier()
        {
            return child.supplier();
        }

        @Override
        public BiConsumer<A, T> accumulator()
        {
            return child.accumulator();
        }

        @Override
        public BinaryOperator<A> combiner()
        {
            return child.combiner();
        }

        @Override
        public Function<A, Map<K, List<T>>> finisher()
        {
            return a -> child.finisher().apply(a).entrySet().stream()
                    .collect(toImmutableMap(Map.Entry::getKey, e -> ImmutableList.copyOf(e.getValue())));
        }

        @Override
        public Set<Characteristics> characteristics()
        {
            return child.characteristics();
        }
    }

    public static <T, K> Collector<T, ?, Map<K, List<T>>> immutableGroupingBy(Function<? super T, ? extends K> classifier)
    {
        return new ImmutableGroupingByCollector<>(groupingBy(classifier));
    }

    public static <T> Collector<T, ?, T> toSingle()
    {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyFunction,
            Function<? super T, ? extends V> valueFunction)
    {
        return com.google.common.collect.ImmutableMap.toImmutableMap(keyFunction, valueFunction);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap()
    {
        return com.google.common.collect.ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyFunction,
            Function<? super T, ? extends V> valueFunction,
            BinaryOperator<V> mergeFunction)
    {
        return com.google.common.collect.ImmutableMap.toImmutableMap(keyFunction, valueFunction, mergeFunction);
    }

    public static <T, K> Collector<T, ?, Map<K, Set<T>>> groupingBySet(Function<? super T, ? extends K> classifier)
    {
        return groupingBy(classifier, LinkedHashMap::new, Collectors.toSet());
    }
}
