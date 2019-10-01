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
package com.wrmsr.tokamak.core.layout.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.Pair;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.UnaryOperator.identity;

@Immutable
public final class Field
{
    private final String name;
    private final Type type;
    private final List<FieldAnnotation> annotations;

    private final Pair<String, Type> nameTypePair;
    private final Map<Class<? extends FieldAnnotation>, FieldAnnotation> annotationsByCls;

    @JsonCreator
    public Field(
            @JsonProperty("name") String name,
            @JsonProperty("type") Type type,
            @JsonProperty("annotations") Iterable<FieldAnnotation> annotations)
    {
        this.name = checkNotEmpty(name);
        this.type = checkNotNull(type);
        this.annotations = ImmutableList.copyOf(annotations);

        nameTypePair = Pair.immutable(name, type);
        annotationsByCls = this.annotations.stream().collect(toImmutableMap(FieldAnnotation::getClass, identity()));
    }

    public Field(String name, Type type)
    {
        this(name, type, ImmutableList.of());
    }

    @Override
    public String toString()
    {
        return "Field{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("type")
    public Type getType()
    {
        return type;
    }

    public Pair<String, Type> getNameTypePair()
    {
        return nameTypePair;
    }

    @JsonProperty("annotations")
    public List<FieldAnnotation> getAnnotations()
    {
        return annotations;
    }

    public Map<Class<? extends FieldAnnotation>, FieldAnnotation> getAnnotationsByCls()
    {
        return annotationsByCls;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends FieldAnnotation> Optional<T> getAnnotation(Class<T> cls)
    {
        return Optional.ofNullable((T) annotationsByCls.get(cls));
    }

    public boolean hasAttribute(Class<? extends FieldAnnotation> cls)
    {
        return annotationsByCls.containsKey(cls);
    }

    public Field withAnnotation(FieldAnnotation... annotations)
    {
        return new Field(name, type,
                Iterables.<FieldAnnotation>concat(this.annotations, Arrays.asList(annotations)));
    }

    @SafeVarargs
    public final Field withoutAnnotation(Class<? extends FieldAnnotation>... annotationClss)
    {
        return new Field(name, type,
                Iterables.filter(annotations, a -> Arrays.stream(annotationClss).anyMatch(ac -> ac.isInstance(a))));
    }

    public Field replacingAnnotation(FieldAnnotation... annotations)
    {
        return new Field(name, type,
                Iterables.concat(Iterables.filter(this.annotations, a -> Arrays.stream(annotations).anyMatch(ac -> ac.getClass().isInstance(a))), Arrays.asList(annotations)));
    }
}
