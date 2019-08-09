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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;

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

    public static <T> T checkSingle(Iterable<T> iterable)
    {
        Iterator<T> it = iterable.iterator();
        checkState(it.hasNext());
        T ret = it.next();
        checkState(!it.hasNext());
        return ret;
    }

    public static <T> T checkSingle(Stream<T> stream)
    {
        return stream.collect(toSingle());
    }

    public static <T> Collection<T> checkNotEmpty(Collection<T> coll)
    {
        checkState(!coll.isEmpty());
        return coll;
    }

    public static <T> List<T> checkNotEmpty(List<T> coll)
    {
        checkState(!coll.isEmpty());
        return coll;
    }

    public static <T> Set<T> checkNotEmpty(Set<T> coll)
    {
        checkState(!coll.isEmpty());
        return coll;
    }

    public static <K, V> Map<K, V> checkNotEmpty(Map<K, V> coll)
    {
        checkState(!coll.isEmpty());
        return coll;
    }

    public static <T> Iterable<T> checkNotEmpty(Iterable<T> iterable)
    {
        checkState(!iterable.iterator().hasNext());
        return iterable;
    }
}
