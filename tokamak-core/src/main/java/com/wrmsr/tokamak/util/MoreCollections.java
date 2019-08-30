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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.util.collect.Ordered;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Sets.newIdentityHashSet;

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

    public static <T> List<Pair<Integer, T>> enumerate(Iterable<T> it)
    {
        List<T> list = ImmutableList.copyOf(it);
        ImmutableList.Builder<Pair<Integer, T>> builder = ImmutableList.builder();
        for (int i = 0; i < list.size(); ++i) {
            builder.add(new Pair.Immutable<>(i, list.get(i)));
        }
        return builder.build();
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
