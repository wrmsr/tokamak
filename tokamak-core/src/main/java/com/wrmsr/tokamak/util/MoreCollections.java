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

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Sets.newIdentityHashSet;

public final class MoreCollections
{
    private MoreCollections()
    {
    }

    public static <E> Set<E> newIdentityHashSetOf(Iterable<E> src)
    {
        Set<E> set = newIdentityHashSet();
        for (E item : src) {
            set.add(item);
        }
        return set;
    }

    public static <T> List<Pair<Integer, T>> enumerate(Iterable<T> it)
    {
        List<T> list = ImmutableList.copyOf(it);
        ImmutableList.Builder<Pair<Integer, T>> builder = ImmutableList.builder();
        for (int i = 0; i < list.size(); ++i) {
            builder.add(new Pair.Immutable<>(i, list.get(i)));
        }
        return builder.build();
    }

    public static <E> List<E> listOf(int size, E value)
    {
        return IntStream.range(0, size).boxed().map(i -> value).collect(toImmutableList());
    }
}
