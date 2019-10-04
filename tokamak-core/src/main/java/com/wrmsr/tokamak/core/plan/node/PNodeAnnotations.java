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
package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.Function.identity;

@Immutable
public final class PNodeAnnotations
        extends AnnotationCollection<PNodeAnnotation, PNodeAnnotations>
{
    @Immutable
    public static final class FieldAnnotations
            extends AnnotationCollection<FieldAnnotation, FieldAnnotations>
    {
        private String field;

        @JsonCreator
        public FieldAnnotations(
                @JsonProperty("field") String field,
                @JsonProperty("annotations") Iterable<FieldAnnotation> annotations)
        {
            super(FieldAnnotation.class, annotations);

            this.field = checkNotEmpty(field);
        }

        public FieldAnnotations(String field)
        {
            this(field, ImmutableList.of());
        }

        @Override
        public String toString()
        {
            return "FieldAnnotations{" +
                    "field='" + field + '\'' +
                    '}';
        }

        @Override
        protected FieldAnnotations rebuildWithAnnotations(Iterable<FieldAnnotation> annotations)
        {
            return new FieldAnnotations(field, annotations);
        }

        @JsonProperty("field")
        public String getField()
        {
            return field;
        }

        @JsonProperty("annotations")
        @Override
        public List<FieldAnnotation> getAnnotations()
        {
            return super.getAnnotations();
        }
    }

    private final Map<String, FieldAnnotations> fieldAnnotationsByField;

    @JsonCreator
    public PNodeAnnotations(
            @JsonProperty("annotations") Iterable<PNodeAnnotation> annotations,
            @JsonProperty("fields") Iterable<FieldAnnotations> fieldAnnotations)
    {
        super(PNodeAnnotation.class, annotations);

        fieldAnnotationsByField = StreamSupport.stream(checkNotNull(fieldAnnotations).spliterator(), false)
                .collect(toImmutableMap(FieldAnnotations::getField, identity()));
    }

    public PNodeAnnotations()
    {
        this(ImmutableList.of(), ImmutableList.of());
    }

    @Override
    protected PNodeAnnotations rebuildWithAnnotations(Iterable<PNodeAnnotation> annotations)
    {
        return new PNodeAnnotations(annotations, getFieldAnnotations());
    }

    protected PNodeAnnotations rebuildWithFieldAnnotations(Iterable<FieldAnnotations> fieldAnnotations)
    {
        return new PNodeAnnotations(annotations, fieldAnnotations);
    }

    @JsonProperty("annotations")
    @Override
    public List<PNodeAnnotation> getAnnotations()
    {
        return super.getAnnotations();
    }

    @JsonProperty("fieldAnnotations")
    public Collection<FieldAnnotations> getFieldAnnotations()
    {
        return fieldAnnotationsByField.values();
    }

    public Map<String, FieldAnnotations> getFieldAnnotationsByField()
    {
        return fieldAnnotationsByField;
    }

    public Optional<FieldAnnotations> getFieldAnnotations(String field)
    {
        return Optional.ofNullable(fieldAnnotationsByField.get(field));
    }

    public final PNodeAnnotations withFieldAnnotation(FieldAnnotation... fieldAnnotations)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.withAnnotation(fieldAnnotations)).collect(toImmutableList()));
    }

    @SafeVarargs
    public final PNodeAnnotations withoutFieldAnnotation(Class<? extends FieldAnnotation>... fieldAnnotationClss)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.withoutAnnotation(fieldAnnotationClss)).collect(toImmutableList()));
    }

    public final PNodeAnnotations overwritingFieldAnnotation(FieldAnnotation... fieldAnnotations)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.overwritingAnnotation(fieldAnnotations)).collect(toImmutableList()));
    }

    public final PNodeAnnotations withFieldAnnotation(String field, FieldAnnotation... fieldAnnotations)
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
    public final PNodeAnnotations withoutFieldAnnotation(String field, Class<? extends FieldAnnotation>... fieldAnnotationClss)
    {
        return rebuildWithFieldAnnotations(getFieldAnnotations().stream()
                .map(fa -> fa.field.equals(field) ? fa.withoutAnnotation(fieldAnnotationClss) : fa)
                .collect(toImmutableList()));
    }

    public final PNodeAnnotations overwritingFieldAnnotation(String field, FieldAnnotation... fieldAnnotations)
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
