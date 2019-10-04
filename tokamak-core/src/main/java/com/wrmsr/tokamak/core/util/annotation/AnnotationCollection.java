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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public abstract class AnnotationCollection<T extends Annotation, Self extends AnnotationCollection<T, Self>>
{
    protected final Class<T> annotationBaseCls;
    protected final List<T> annotations;

    protected final Map<Class<? extends T>, T> annotationsByCls;

    @SuppressWarnings({"unchecked"})
    protected AnnotationCollection(Class<T> annotationBaseCls, Iterable<T> annotations)
    {
        this.annotationBaseCls = checkNotNull(annotationBaseCls);
        this.annotations = ImmutableList.copyOf(annotations);

        this.annotations.forEach(a -> checkArgument(annotationBaseCls.isInstance(a)));
        annotationsByCls = (Map) this.annotations.stream().collect(toImmutableMap(Annotation::getClass, identity()));
    }

    protected AnnotationCollection(Class<T> annotationBaseCls)
    {
        this(annotationBaseCls, ImmutableList.of());
    }

    public List<T> getAnnotations()
    {
        return annotations;
    }

    public Map<Class<? extends T>, T> getAnnotationsByCls()
    {
        return annotationsByCls;
    }

    protected abstract Self rebuildWithAnnotations(Iterable<T> annotations);

    @SuppressWarnings({"unchecked"})
    public <U extends T> Optional<T> getAnnotation(Class<U> cls)
    {
        return Optional.ofNullable((U) annotationsByCls.get(cls));
    }

    public boolean hasAnnotation(Class<? extends T> cls)
    {
        return annotationsByCls.containsKey(cls);
    }

    @SafeVarargs
    public final Self withAnnotation(T... annotations)
    {
        return rebuildWithAnnotations(Iterables.<T>concat(this.annotations, Arrays.asList(annotations)));
    }

    @SafeVarargs
    public final Self withoutAnnotation(Class<? extends T>... annotationClss)
    {
        return rebuildWithAnnotations(
                Iterables.filter(annotations, a -> Arrays.stream(annotationClss).anyMatch(ac -> ac.isInstance(a))));
    }

    @SafeVarargs
    public final Self overwritingAnnotation(T... annotations)
    {
        return rebuildWithAnnotations(
                Iterables.concat(Iterables.filter(this.annotations, a -> Arrays.stream(annotations).anyMatch(ac -> ac.getClass().isInstance(a))), Arrays.asList(annotations)));
    }
}
