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
package com.wrmsr.tokamak.core.type.hier.special;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.TypeUtils;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;

import javax.annotation.concurrent.Immutable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.sorted;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class AnnotatedType
        implements SpecialType
{
    public static final String NAME = "Annotated";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, AnnotatedType.class, TypeConstructor.of(
            (List<Object> args) -> unpack(TypeUtils.objectsToTypes(args))));

    private final AnnotationCollection<TypeAnnotation> annotations;
    private final Type item;

    public AnnotatedType(Iterable<TypeAnnotation> annotations, Type item)
    {
        this.annotations = AnnotationCollection.copyOf(sorted(annotations, Comparator.comparing(TypeAnnotation::getName)));
        this.item = checkNotNull(item);
        checkNotEmpty(this.annotations);
        checkState(!(item instanceof AnnotatedType));
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public List<Object> getArgs()
    {
        return ImmutableList.builder().addAll(annotations).add(item).build();
    }

    public AnnotationCollection<TypeAnnotation> getAnnotations()
    {
        return annotations;
    }

    public Type getItem()
    {
        return item;
    }

    public static Type of(Iterable<TypeAnnotation> annotations, Type type)
    {
        return of(ImmutableList.copyOf(annotations), type);
    }

    public static Type of(List<TypeAnnotation> annotations, Type type)
    {
        ImmutableMap.Builder<Class<? extends TypeAnnotation>, TypeAnnotation> builder = ImmutableMap.builder();
        annotations.forEach(ann -> builder.put(ann.getClass(), ann));
        while (type instanceof AnnotatedType) {
            AnnotatedType annotatedType = (AnnotatedType) type;
            builder.putAll(annotatedType.annotations.getByCls());
            type = annotatedType.item;
        }
        Map<Class<? extends TypeAnnotation>, TypeAnnotation> map = builder.build();
        if (map.isEmpty()) {
            return new AnnotatedType(map.values(), type);
        }
        else {
            return type;
        }
    }

    public static Type unpack(List<Type> args)
    {
        checkNotEmpty(args);
        return of(
                args.subList(0, args.size() - 1).stream()
                        .map(TypeAnnotation.class::cast)
                        .collect(toImmutableList()),
                args.get(args.size() - 1));
    }
}
