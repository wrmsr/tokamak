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
import com.google.common.collect.Iterables;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public abstract class AnnotationCollectionMap<
        K,
        T extends Annotation,
        E extends AnnotationCollectionMap.Entry<K, T, E>,
        Self extends AnnotationCollectionMap<K, T, E, Self>>
{
    @Immutable
    protected static abstract class Entry<K, T extends Annotation, Self extends AnnotationCollectionMap.Entry<K, T, Self>>
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

    protected final Map<K, E> entriesByKey;

    protected AnnotationCollectionMap(Iterable<E> entries)
    {
        entriesByKey = StreamSupport.stream(checkNotNull(entries).spliterator(), false)
                .collect(toImmutableMap(Entry::getKey, identity()));
    }

    protected abstract Self rebuildWithEntries(Iterable<E> entries);

    protected abstract E newEntry(K key, Iterable<T> annotations);

    public Collection<E> getEntries()
    {
        return entriesByKey.values();
    }

    public Map<K, E> getEntriesByKey()
    {
        return entriesByKey;
    }

    public Optional<E> getEntry(K key)
    {
        return Optional.ofNullable(entriesByKey.get(key));
    }

    @SafeVarargs
    public final Self withAnnotation(T... annotations)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.withAnnotation(annotations)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self withoutAnnotation(Class<? extends T>... annotationClss)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.withoutAnnotation(annotationClss)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self overwritingAnnotation(T... annotations)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.overwritingAnnotation(annotations)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self withAnnotation(K key, T... annotations)
    {
        if (entriesByKey.containsKey(key)) {
            return rebuildWithEntries(getEntries().stream()
                    .map(fa -> fa.getKey().equals(key) ? fa.withAnnotation(annotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithEntries(Iterables.concat(
                    getEntries(),
                    ImmutableList.of(newEntry(key, Arrays.asList(annotations)))));
        }
    }

    @SafeVarargs
    public final Self withoutAnnotation(K key, Class<? extends T>... annotationClss)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.getKey().equals(key) ? fa.withoutAnnotation(annotationClss) : fa)
                .collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self overwritingAnnotation(K key, T... annotations)
    {
        if (entriesByKey.containsKey(key)) {
            return rebuildWithEntries(getEntries().stream()
                    .map(fa -> fa.getKey().equals(key) ? fa.overwritingAnnotation(annotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithEntries(Iterables.concat(
                    getEntries(),
                    ImmutableList.of(newEntry(key, Arrays.asList(annotations)))));
        }
    }

    @SafeVarargs
    public final Self withAnnotation(Iterable<K> keys, T... annotations)
    {
        @SuppressWarnings({"unchecked"})
        Self ret = (Self) this;
        for (K key : keys) {
            ret = ret.withAnnotation(key, annotations);
        }
        return ret;
    }

    @SafeVarargs
    public final Self withoutAnnotation(Iterable<K> keys, Class<? extends T>... annotationClss)
    {
        @SuppressWarnings({"unchecked"})
        Self ret = (Self) this;
        for (K key : keys) {
            ret = ret.withoutAnnotation(key, annotationClss);
        }
        return ret;
    }

    @SafeVarargs
    public final Self overwritingAnnotation(Iterable<K> keys, T... annotations)
    {
        @SuppressWarnings({"unchecked"})
        Self ret = (Self) this;
        for (K key : keys) {
            ret = ret.overwritingAnnotation(key, annotations);
        }
        return ret;
    }
}
