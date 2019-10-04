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
public abstract class AnnotationCollectionMap<K, T extends Annotation, Self extends AnnotationCollectionMap<K, T, Self>>
{
    @Immutable
    public static abstract class Entry<K, T extends Annotation>
            extends AnnotationCollection<T, AnnotationCollectionMap.Entry<K, T>>
    {
        private K key;

        public Entry(K key, Iterable<T> annotations)
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

    private final Map<K, Entry<K, T>> entriesByKey;

    public AnnotationCollectionMap(Iterable<Entry<K, T>> entries)
    {
        entriesByKey = StreamSupport.stream(checkNotNull(entries).spliterator(), false)
                .collect(toImmutableMap(Entry::getKey, identity()));
    }

    protected abstract Self rebuildWithEntries(Iterable<Entry<K, T>> entries);

    protected abstract Entry<K, T> newEntry(K key, Iterable<T> annotations);

    public Collection<Entry<K, T>> getEntries()
    {
        return entriesByKey.values();
    }

    public Map<K, Entry<K, T>> getEntriesByKey()
    {
        return entriesByKey;
    }

    public Optional<Entry> getEntry(K key)
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
    public final Self withFieldAnnotation(K key, T... annotations)
    {
        if (entriesByKey.containsKey(key)) {
            return rebuildWithEntries(getEntries().stream()
                    .map(fa -> fa.key.equals(key) ? fa.withAnnotation(annotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithEntries(Iterables.concat(
                    getEntries(),
                    ImmutableList.of(newEntry(key, Arrays.asList(annotations)))));
        }
    }

    @SafeVarargs
    public final Self withoutFieldAnnotation(K key, Class<? extends T>... annotationClss)
    {
        return rebuildWithEntries(getEntries().stream()
                .map(fa -> fa.key.equals(key) ? fa.withoutAnnotation(annotationClss) : fa)
                .collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self overwritingFieldAnnotation(K key, T... annotations)
    {
        if (entriesByKey.containsKey(key)) {
            return rebuildWithEntries(getEntries().stream()
                    .map(fa -> fa.key.equals(key) ? fa.overwritingAnnotation(annotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithEntries(Iterables.concat(
                    getEntries(),
                    ImmutableList.of(newEntry(key, Arrays.asList(annotations)))));
        }
    }
}
