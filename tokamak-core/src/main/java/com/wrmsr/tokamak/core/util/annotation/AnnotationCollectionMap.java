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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.function.Function.identity;

@Immutable
public abstract class AnnotationCollectionMap<K, T extends Annotation, Self extends AnnotationCollectionMap<K, T, Self>>
{
    @Immutable
    public static abstract class Entry<K, T extends Annotation>
            extends AnnotationCollection<T, AnnotationCollectionMap.Entry<K, T>>
    {
        private K key;

        public Entry(Class<T> annotationBaseCls, K key, Iterable<T> annotations)
        {
            super(annotationBaseCls, annotations);

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

    public AnnotationCollectionMap(Iterable<Entry<K ,T>> entries)
    {
        entriesByKey = StreamSupport.stream(checkNotNull(entries).spliterator(), false)
                .collect(toImmutableMap(Entry::getKey, identity()));
    }

    protected abstract Self rebuildWithEntries(Iterable<Entry<K, T>> entries);

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

    public final Self withFieldAnnotation(FieldAnnotation... fieldAnnotations)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.withAnnotation(fieldAnnotations)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self withoutFieldAnnotation(Class<? extends FieldAnnotation>... fieldAnnotationClss)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.withoutAnnotation(fieldAnnotationClss)).collect(toImmutableList()));
    }

    public final Self overwritingFieldAnnotation(FieldAnnotation... fieldAnnotations)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.overwritingAnnotation(fieldAnnotations)).collect(toImmutableList()));
    }

    public final Self withFieldAnnotation(String field, FieldAnnotation... fieldAnnotations)
    {
        if (fieldAnnotationsByField.containsKey(field)) {
            return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                    .map(fa -> fa.field.equals(field) ? fa.withAnnotation(fieldAnnotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithFieldAnnotations(Iterables.concat(
                    getFieldAnnotations(),
                    ImmutableList.of(new FieldAnnotations(field, Arrays.asList(fieldAnnotations)))));
        }
    }

    @SafeVarargs
    public final Self withoutFieldAnnotation(String field, Class<? extends FieldAnnotation>... fieldAnnotationClss)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.field.equals(field) ? fa.withoutAnnotation(fieldAnnotationClss) : fa)
                .collect(toImmutableList()));
    }

    public final Self overwritingFieldAnnotation(String field, FieldAnnotation... fieldAnnotations)
    {
        if (fieldAnnotationsByField.containsKey(field)) {
            return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                    .map(fa -> fa.field.equals(field) ? fa.overwritingAnnotation(fieldAnnotations) : fa)
                    .collect(toImmutableList()));
        }
        else {
            return rebuildWithFieldAnnotations(Iterables.concat(
                    getFieldAnnotations(),
                    ImmutableList.of(new FieldAnnotations(field, Arrays.asList(fieldAnnotations)))));
        }
    }
}
