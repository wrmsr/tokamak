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
package com.wrmsr.tokamak.core.util.annotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.wrmsr.tokamak.util.collect.StreamableIterable;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableListMap;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public abstract class AnnotationCollectionMap<
        K,
        T extends Annotation,
        E extends AnnotationCollectionMap.Entry<K, T, E>,
        Self extends AnnotationCollectionMap<K, T, E, Self>>
        implements StreamableIterable<E>
{
    @Immutable
    public static abstract class Entry<K, T extends Annotation, Self extends AnnotationCollectionMap.Entry<K, T, Self>>
            extends AnnotationCollection<T, Self>
    {
        protected K key;

        protected Entry(K key, Iterable<T> annotations)
        {
            super(annotations);

            this.key = checkNotNull(key);
        }

        @Override
        public String toString()
        {
            return "Entry{" +
                    "key=" + key +
                    '}';
        }

        public K getKey()
        {
            return key;
        }
    }

    protected final List<E> entries;

    protected final Map<K, E> entriesByKey;

    protected AnnotationCollectionMap(Iterable<E> entries)
    {
        this.entries = ImmutableList.copyOf(entries);

        entriesByKey = this.entries.stream()
                .collect(toImmutableMap(Entry::getKey, identity()));
    }

    protected abstract Self rebuildWithEntries(Iterable<E> entries);

    protected abstract E newEntry(K key, Iterable<T> annotations);

    public List<E> getEntries()
    {
        return entries;
    }

    @Override
    public Iterator<E> iterator()
    {
        return entries.iterator();
    }

    public Map<K, E> getEntriesByKey()
    {
        return entriesByKey;
    }

    public boolean containsKey(K key)
    {
        return entriesByKey.containsKey(key);
    }

    public Optional<E> getEntry(K key)
    {
        return Optional.ofNullable(entriesByKey.get(key));
    }

    public Optional<AnnotationCollection<T, ?>> get(K key)
    {
        return Optional.ofNullable(entriesByKey.get(checkNotNull(key)));
    }

    public AnnotationCollection<T, ?> getOrEmpty(K key)
    {
        return get(key).orElseGet(() -> newEntry(key, ImmutableList.of()));
    }

    private final SupplierLazyValue<Map<Class<? extends T>, List<E>>> entryListsByAnnotationCls = new SupplierLazyValue<>();

    public Map<Class<? extends T>, List<E>> getEntryListsByAnnotationCls()
    {
        return entryListsByAnnotationCls.get(() -> {
            Map<Class<? extends T>, List<E>> listsByCls = new LinkedHashMap<>();
            entries.forEach(e -> e.getByCls().keySet().forEach(ac -> listsByCls.computeIfAbsent(ac, ac_ -> new ArrayList<>()).add(e)));
            return newImmutableListMap(listsByCls);
        });
    }

    public boolean containsAnnotation(Class<? extends T> cls)
    {
        return entries.stream().anyMatch(e -> e.contains(cls));
    }

    public E getEntryOrEmpty(K key)
    {
        return getEntry(key).orElseGet(() -> newEntry(key, ImmutableList.of()));
    }

    @SafeVarargs
    public final Self with(T... annotations)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.with(annotations)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self without(Class<? extends T>... annotationClss)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.without(annotationClss)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self overwriting(T... annotations)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.overwriting(annotations)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self with(K key, T... annotations)
    {
        if (entriesByKey.containsKey(key)) {
            return rebuildWithEntries(getEntries().stream()
                    .map(fa -> fa.getKey().equals(key) ? fa.with(annotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithEntries(Iterables.concat(
                    getEntries(),
                    ImmutableList.of(newEntry(key, Arrays.asList(annotations)))));
        }
    }

    @SafeVarargs
    public final Self without(K key, Class<? extends T>... annotationClss)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.getKey().equals(key) ? fa.without(annotationClss) : fa)
                .collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self overwriting(K key, T... annotations)
    {
        if (entriesByKey.containsKey(key)) {
            return rebuildWithEntries(getEntries().stream()
                    .map(fa -> fa.getKey().equals(key) ? fa.overwriting(annotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithEntries(Iterables.concat(
                    getEntries(),
                    ImmutableList.of(newEntry(key, Arrays.asList(annotations)))));
        }
    }

    @SafeVarargs
    public final Self with(Iterable<K> keys, T... annotations)
    {
        checkArgument(annotations.length > 0);
        @SuppressWarnings({"unchecked"})
        Self ret = (Self) this;
        for (K key : keys) {
            ret = ret.with(key, annotations);
        }
        return ret;
    }

    @SafeVarargs
    public final Self without(Iterable<K> keys, Class<? extends T>... annotationClss)
    {
        checkArgument(annotationClss.length > 0);
        @SuppressWarnings({"unchecked"})
        Self ret = (Self) this;
        for (K key : keys) {
            ret = ret.without(key, annotationClss);
        }
        return ret;
    }

    @SafeVarargs
    public final Self overwriting(Iterable<K> keys, T... annotations)
    {
        checkArgument(annotations.length > 0);
        @SuppressWarnings({"unchecked"})
        Self ret = (Self) this;
        for (K key : keys) {
            ret = ret.overwriting(key, annotations);
        }
        return ret;
    }

    public final Self withoutKeys(Set<K> keys)
    {
        return rebuildWithEntries(getEntries().stream()
                .filter(e -> !keys.contains(e.key))
                .collect(toImmutableList()));
    }

    public final Self withoutKeys(Iterable<K> keys)
    {
        return withoutKeys(ImmutableSet.copyOf(keys));
    }

    @SafeVarargs
    public final Self withoutKeys(K... keys)
    {
        return withoutKeys(ImmutableSet.copyOf(keys));
    }
}
