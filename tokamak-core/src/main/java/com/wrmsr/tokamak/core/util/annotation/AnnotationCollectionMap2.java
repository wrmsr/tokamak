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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.collect.AbstractUnmodifiableMap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableListMap;

@Immutable
public abstract class AnnotationCollectionMap2<
        K,
        T extends Annotation,
        C extends AnnotationCollection<T, C>>
        extends AbstractUnmodifiableMap<K, C>
{
    protected final Map<K, C> map;

    protected AnnotationCollectionMap2(Map<K, C> map)
    {
        this.map = ImmutableMap.copyOf(map);
    }

    protected abstract Self rebuild(Iterable<E> entries);

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

    public Self filterAnnotations(Predicate<T> predicate)
    {
        return rebuild(entries.stream().map(e -> e.filter(predicate)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self appendAnnotations(T... annotations)
    {
        return rebuild(entries.stream().map(e -> e.append(annotations)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self dropAnnotations(Class<? extends T>... annotationClss)
    {
        return rebuild(entries.stream().map(e -> e.drop(annotationClss)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self updateAnnotations(T... annotations)
    {
        return rebuild(entries.stream().map(e -> e.update(annotations)).collect(toImmutableList()));
    }
}
