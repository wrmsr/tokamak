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

package com.wrmsr.tokamak.core.type.impl;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.TypeAnnotation;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class AnnotatedType
        extends AbstractType
{
    public static final String NAME = "Annotated";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, AnnotatedType.class, TypeConstructor.of(AnnotatedType::new));

    private final AnnotationCollection<TypeAnnotation> annotations;

    public AnnotatedType(List<Object> args)
    {
        super(NAME, hoistArgs(args));
        checkArgument(this.args.size() >= 1);
        checkArgument(this.args.get(this.args.size() - 1) instanceof Type);
        annotations = AnnotationCollection.copyOf(
                this.args.subList(0, this.args.size() - 1).stream()
                        .map(a -> {
                            checkArgument(a instanceof TypeAnnotation);
                            return (TypeAnnotation) a;
                        })
                        .collect(toImmutableList()));
    }

    public AnnotationCollection<TypeAnnotation> getAnnotations()
    {
        return annotations;
    }

    private static List<Object> hoistArgs(List<Object> args)
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
            checkArgument(args.get(args.size() - 1) instanceof Type);
            type = (Type) args.get(args.size() - 1);
            if (!(type instanceof AnnotatedType)) {
                break;
            }
            args = type.getArgs();
        }
        return ImmutableList.builder().addAll(anns.values()).add(type).build();
    }
}
