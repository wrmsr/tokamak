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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.collect.AbstractUnmodifiableMap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;

@Immutable
public final class AnnotationCollectionMap<K, T extends Annotation>
        extends AbstractUnmodifiableMap<K, AnnotationCollection<T>>
{
    protected final Map<K, AnnotationCollection<T>> map;

    @JsonCreator
    public AnnotationCollectionMap(
            @JsonProperty("map") Map<K, AnnotationCollection<T>> map)
    {
        this.map = ImmutableMap.copyOf(map);
    }

    public static <K, T extends Annotation> AnnotationCollectionMap<K, T> of()
    {
        return new AnnotationCollectionMap<>(ImmutableMap.of());
    }

    @SuppressWarnings({"unchecked"})
    public static <K, T extends Annotation> AnnotationCollectionMap<K, T> copyOf(Map<K, Iterable<T>> map)
    {
        return map instanceof AnnotationCollectionMap ? (AnnotationCollectionMap) map :
                new AnnotationCollectionMap<>(immutableMapValues(map, AnnotationCollection::copyOf));
    }

    @SuppressWarnings({"unchecked"})
    public static <K, T extends Annotation> AnnotationCollectionMap<K, T> merge(Iterable<Map<K, AnnotationCollection<T>>> annotationCollectionMaps)
    {
        Map<K, Map<Class<? extends T>, T>> ret = new LinkedHashMap<>();
        for (Map<K, AnnotationCollection<T>> map : annotationCollectionMaps) {
            for (Map.Entry<K, AnnotationCollection<T>> entry : map.entrySet()) {
                Map<Class<? extends T>, T> retMap = ret.computeIfAbsent(entry.getKey(), k_ -> new LinkedHashMap<>());
                for (T ann : entry.getValue()) {
                    retMap.put((Class<? extends T>) ann.getClass(), ann);
                }
            }
        }
        return new AnnotationCollectionMap<>(immutableMapValues(ret, retMap -> new AnnotationCollection<>(retMap.values())));
    }

    public static <K, T extends Annotation> AnnotationCollectionMap<K, T> mergeOf(Map<K, AnnotationCollection<T>>... annotationCollectionMaps)
    {
        return merge(Arrays.asList(annotationCollectionMaps));
    }

    @Override
    public String toString()
    {
        return "AnnotationCollectionMap{" +
                "map=" + map +
                '}';
    }

    @JsonProperty("map")
    public Map<K, AnnotationCollection<T>> getMap()
    {
        return map;
    }

    @Override
    public Set<Entry<K, AnnotationCollection<T>>> entrySet()
    {
        return map.entrySet();
    }

    @Override
    public AnnotationCollection<T> getOrDefault(Object key, AnnotationCollection<T> defaultValue)
    {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super AnnotationCollection<T>> action)
    {
        map.forEach(action);
    }

    private final SupplierLazyValue<Map<Class<? extends T>, Set<K>>> keySetsByAnnotationCls = new SupplierLazyValue<>();

    @SuppressWarnings({"unchecked"})
    public Map<Class<? extends T>, Set<K>> getKeySetsByAnnotationCls()
    {
        return keySetsByAnnotationCls.get(() -> {
            Map<Class<? extends T>, ImmutableSet.Builder<K>> buildersByCls = new LinkedHashMap<>();
            map.forEach((k, c) -> c.forEach(a -> buildersByCls.computeIfAbsent((Class<? extends T>) a.getClass(), ac -> ImmutableSet.<K>builder()).add(k)));
            return immutableMapValues(buildersByCls, ImmutableSet.Builder::build);
        });
    }

    public boolean containsAnnotation(Class<? extends T> cls)
    {
        return map.values().stream().anyMatch(c -> c.contains(cls));
    }

    public AnnotationCollectionMap<K, T> filter(Predicate<T> predicate)
    {
        return new AnnotationCollectionMap<>(immutableMapValues(map, c -> c.filter(predicate)));
    }

    @SafeVarargs
    public final AnnotationCollectionMap<K, T> append(T... annotations)
    {
        return new AnnotationCollectionMap<>(immutableMapValues(map, e -> e.append(annotations)));
    }

    @SafeVarargs
    public final AnnotationCollectionMap<K, T> drop(Class<? extends T>... annotationClss)
    {
        return new AnnotationCollectionMap<>(immutableMapValues(map, e -> e.drop(annotationClss)));
    }

    @SafeVarargs
    public final AnnotationCollectionMap<K, T> update(T... annotations)
    {
        return new AnnotationCollectionMap<>(immutableMapValues(map, e -> e.update(annotations)));
    }
}
