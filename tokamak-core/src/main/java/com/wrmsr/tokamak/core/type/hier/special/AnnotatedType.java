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
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.util.Pair;

import javax.annotation.concurrent.Immutable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class AnnotatedType
        implements SpecialType
{
    public static final String NAME = "Annotated";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, AnnotatedType.class, TypeConstructor.of(
            (List<Object> args) -> {
                Pair<AnnotationCollection<TypeAnnotation>, Type> flattened = flattenArgs(Types.objectsToTypes(args));
                return new AnnotatedType(flattened.first(), flattened.second());
            }));

    private final AnnotationCollection<TypeAnnotation> annotations;
    private final Type item;

    public AnnotatedType(Iterable<TypeAnnotation> annotations, Type item)
    {
        this.annotations = AnnotationCollection.copyOf(annotations);
        this.item = checkNotNull(item);
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

    public static Pair<AnnotationCollection<TypeAnnotation>, Type> flattenArgs(List<Type> args)
    {
        Map<Class<? extends TypeAnnotation>, TypeAnnotation> anns = new LinkedHashMap<>();
        Type type;
        args = ImmutableList.copyOf(args);
        while (true) {
            checkNotEmpty(args);
            for (int i = 0; i < args.size() - 1; ++i) {
                checkArgument(args.get(i) instanceof TypeAnnotation);
                TypeAnnotation ann = (TypeAnnotation) args.get(i);
                TypeAnnotation existing = anns.get(ann.getClass());
                if (existing != null) {
                    throw new IllegalArgumentException(String.format("Duplicate type annotation types: %s, %s", ann, existing));
                }
                anns.put(ann.getClass(), ann);
            }
            type = (Type) args.get(args.size() - 1);
            if (!(type instanceof AnnotatedType)) {
                break;
            }
            args = Types.objectsToTypes(type.getArgs());
        }
        return Pair.immutable(AnnotationCollection.copyOf(anns.values()), type);
    }
}
