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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toCheckSingle;

public final class MorePreconditions
{
    private MorePreconditions()
    {
    }

    public static <T> Iterable<T> checkUnique(Iterable<T> iterable)
    {
        Set<T> seen = new HashSet<>();
        for (T item : iterable) {
            if (!seen.add(item)) {
                throw new IllegalStateException();
            }
        }
        return iterable;
    }

    public static <T> Iterable<T> checkUnique(Stream<T> stream)
    {
        return checkUnique(stream.collect(toImmutableList()));
    }

    public static <T> Collection<T> checkUnique(Collection<T> collection)
    {
        checkUnique((Iterable<T>) collection);
        return collection;
    }

    public static <T> List<T> checkUnique(List<T> list)
    {
        checkUnique((Iterable<T>) list);
        return list;
    }

    public static <T> T checkContains(T item, Iterable<T> iterable)
    {
        for (T itItem : iterable) {
            if (item.equals(itItem)) {
                return item;
            }
        }
        throw new IllegalStateException(String.format("%s not in %s", item, iterable));
    }

    public static <T> T checkContains(T item, Collection<T> collection)
    {
        if (!collection.contains(item)) {
            throw new IllegalStateException(String.format("%s not in %s", item, collection));
        }
        return item;
    }

    public static <T> T checkNotContains(T item, Iterable<T> iterable)
    {
        for (T itItem : iterable) {
            if (item.equals(itItem)) {
                throw new IllegalStateException(String.format("%s in %s", item, iterable));
            }
        }
        return item;
    }

    public static <T> T checkNotContains(T item, Collection<T> collection)
    {
        if (collection.contains(item)) {
            throw new IllegalStateException(String.format("%s in %s", item, collection));
        }
        return item;
    }

    public static <T> void checkEmpty(Iterator<T> iterator)
    {
        checkState(!iterator.hasNext());
    }

    public static <I extends Iterable<?>> I checkEmpty(I iterable)
    {
        checkEmpty(iterable.iterator());
        return iterable;
    }

    public static <T> T checkSingle(Iterator<T> iterator)
    {
        checkState(iterator.hasNext());
        T ret = iterator.next();
        checkState(!iterator.hasNext());
        return ret;
    }

    public static <T> T checkSingle(Iterable<T> iterable)
    {
        return checkSingle(iterable.iterator());
    }

    public static <T> T checkSingle(Stream<T> stream)
    {
        return stream.collect(toCheckSingle());
    }

    public static String checkNotEmpty(String obj)
    {
        checkNotNull(obj);
        checkState(!obj.isEmpty());
        return obj;
    }

    public static <T> Collection<T> checkNotEmpty(Collection<T> obj)
    {
        checkNotNull(obj);
        checkState(!obj.isEmpty());
        return obj;
    }

    public static <T> List<T> checkNotEmpty(List<T> obj)
    {
        checkNotNull(obj);
        checkState(!obj.isEmpty());
        return obj;
    }

    public static <T> Set<T> checkNotEmpty(Set<T> obj)
    {
        checkNotNull(obj);
        checkState(!obj.isEmpty());
        return obj;
    }

    public static <K, V> Map<K, V> checkNotEmpty(Map<K, V> obj)
    {
        checkNotNull(obj);
        checkState(!obj.isEmpty());
        return obj;
    }

    public static <T> Iterable<T> checkNotEmpty(Iterable<T> iterable)
    {
        checkNotNull(iterable);
        checkState(iterable.iterator().hasNext());
        return iterable;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Class<? extends T> checkSubclass(Class<?> cls, Class<? extends T> supcls)
    {
        checkArgument(supcls.isAssignableFrom(cls));
        return (Class<? extends T>) cls;
    }
}
