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
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public abstract class AnnotationCollection<T extends Annotation, Self extends AnnotationCollection<T, Self>>
        implements StreamableIterable<T>
{
    protected final List<T> annotations;

    protected final Map<Class<? extends T>, T> annotationsByCls;

    @SuppressWarnings({"unchecked"})
    protected AnnotationCollection(Iterable<T> annotations)
    {
        this.annotations = ImmutableList.copyOf(annotations);

        this.annotations.forEach(a -> checkArgument(getBaseCls().isInstance(a)));
        annotationsByCls = (Map) this.annotations.stream().collect(toImmutableMap(Annotation::getClass, identity()));
    }

    public List<T> get()
    {
        return annotations;
    }

    public Map<Class<? extends T>, T> getByCls()
    {
        return annotationsByCls;
    }

    @Override
    public Iterator<T> iterator()
    {
        return annotations.iterator();
    }

    public int size()
    {
        return annotations.size();
    }

    public boolean isEmpty()
    {
        return annotations.isEmpty();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "annotations=" + annotations +
                '}';
    }

    public abstract Class<T> getBaseCls();

    protected abstract Self rebuildWith(Iterable<T> annotations);

    @SuppressWarnings({"unchecked"})
    public <U extends T> Optional<T> get(Class<U> cls)
    {
        return Optional.ofNullable((U) annotationsByCls.get(cls));
    }

    public boolean contains(Class<? extends T> cls)
    {
        return annotationsByCls.containsKey(cls);
    }

    public Self filter(Predicate<T> predicate)
    {
        return rebuildWith(stream().filter(predicate).collect(toImmutableList()));
    }

    @SafeVarargs
    public final Self with(T... annotations)
    {
        return rebuildWith(Iterables.concat(this.annotations, Arrays.asList(annotations)));
    }

    @SafeVarargs
    public final Self without(Class<? extends T>... annotationClss)
    {
        return rebuildWith(
                Iterables.filter(annotations, a -> Arrays.stream(annotationClss).noneMatch(ac -> ac.isInstance(a))));
    }

    @SafeVarargs
    public final Self overwriting(T... annotations)
    {
        return rebuildWith(
                Iterables.concat(Iterables.filter(this.annotations, a -> Arrays.stream(annotations).anyMatch(ac -> ac.getClass().isInstance(a))), Arrays.asList(annotations)));
    }
}
