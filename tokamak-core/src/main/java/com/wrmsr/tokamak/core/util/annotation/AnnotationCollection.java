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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public final class AnnotationCollection<T extends Annotation>
        implements StreamableIterable<T>
{
    private final List<T> list;

    private final Map<Class<? extends T>, T> annotationsByCls;

    @SuppressWarnings({"unchecked"})
    @JsonCreator
    public AnnotationCollection(
            @JsonProperty("list") Iterable<T> list)
    {
        this.list = ImmutableList.copyOf(list);

        annotationsByCls = (Map) this.list.stream().collect(toImmutableMap(Annotation::getClass, identity()));
    }

    public static <T extends Annotation> AnnotationCollection<T> of()
    {
        return new AnnotationCollection<>(ImmutableList.of());
    }

    @SafeVarargs
    public static <T extends Annotation> AnnotationCollection<T> of(T... annotations)
    {
        return new AnnotationCollection<>(ImmutableList.copyOf(annotations));
    }

    public static <T extends Annotation> AnnotationCollection<T> copyOf(Iterable<T> annotations)
    {
        return annotations instanceof AnnotationCollection ? (AnnotationCollection<T>) annotations : new AnnotationCollection<>(annotations);
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends Annotation> AnnotationCollection<T> merge(Iterable<Iterable<T>> annotationCollections)
    {
        Map<Class<? extends T>, T> ret = new LinkedHashMap<>();
        for (Iterable<T> coll : annotationCollections) {
            for (T ann : coll) {
                ret.put((Class<? extends T>) ann.getClass(), ann);
            }
        }
        return new AnnotationCollection<>(ret.values());
    }

    @SafeVarargs
    public static <T extends Annotation> AnnotationCollection<T> mergeOf(Iterable<T>... annotationCollections)
    {
        return merge(Arrays.asList(annotationCollections));
    }

    @JsonProperty("list")
    public List<T> getList()
    {
        return list;
    }

    public Map<Class<? extends T>, T> getByCls()
    {
        return annotationsByCls;
    }

    @Override
    public Iterator<T> iterator()
    {
        return list.iterator();
    }

    public int size()
    {
        return list.size();
    }

    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    @Override
    public String toString()
    {
        return "AnnotationCollection{" +
                "annotations=" + list +
                '}';
    }

    @SuppressWarnings({"unchecked"})
    public <U extends T> Optional<T> get(Class<U> cls)
    {
        return Optional.ofNullable((U) annotationsByCls.get(cls));
    }

    public boolean contains(Class<? extends T> cls)
    {
        return annotationsByCls.containsKey(cls);
    }

    public AnnotationCollection<T> filtered(Predicate<T> predicate)
    {
        return new AnnotationCollection<>(stream().filter(predicate).collect(toImmutableList()));
    }

    @SafeVarargs
    public final AnnotationCollection<T> appended(T... annotations)
    {
        return new AnnotationCollection<>(Iterables.concat(this.list, Arrays.asList(annotations)));
    }

    @SafeVarargs
    public final AnnotationCollection<T> dropped(Class<? extends T>... annotationClss)
    {
        return new AnnotationCollection<>(
                Iterables.filter(list, a -> Arrays.stream(annotationClss).noneMatch(ac -> ac.isInstance(a))));
    }

    @SafeVarargs
    public final AnnotationCollection<T> updated(T... annotations)
    {
        return new AnnotationCollection<>(
                Iterables.concat(
                        Iterables.filter(this.list, a -> Arrays.stream(annotations).anyMatch(ac -> ac.getClass().isInstance(a))),
                        Arrays.asList(annotations)));
    }

    @SafeVarargs
    public final AnnotationCollection<T> merged(Iterable<T>... annotationCollections)
    {
        return merge(ImmutableList.<Iterable<T>>builder().add(this).add(annotationCollections).build());
    }
}
