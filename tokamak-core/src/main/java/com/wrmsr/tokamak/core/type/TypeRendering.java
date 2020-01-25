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
package com.wrmsr.tokamak.core.type;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.type.hier.TypeLike;
import com.wrmsr.tokamak.core.type.hier.special.AnnotatedType;
import com.wrmsr.tokamak.util.box.Box;
import com.wrmsr.tokamak.util.func.RecursiveFunction;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class TypeRendering
{
    private TypeRendering()
    {
    }

    private static final class Literal
            extends Box<String>
    {
        public Literal(String value)
        {
            super(value);
        }
    }

    public static String buildSpec(Object object)
    {
        if (object instanceof AnnotatedType) {
            return buildSpec((AnnotatedType) object);
        }
        else if (object instanceof TypeLike) {
            return buildSpec((TypeLike) object);
        }
        else if (object instanceof Long) {
            return Long.toString((Long) object);
        }
        else if (object instanceof Literal) {
            return ((Literal) object).getValue();
        }
        else {
            throw new IllegalStateException(Objects.toString(object));
        }
    }

    public static String buildSpec(AnnotatedType annotatedType)
    {
        List<TypeAnnotation> annotations = annotatedType.getAnnotations().getList();
        return RecursiveFunction.applyRecursive((rec, pos) -> {
            if (pos < annotations.size()) {
                TypeAnnotation ann = annotations.get(pos);
                return buildSpec(
                        ann.getName(),
                        ImmutableList.builder()
                                .add(new Literal(rec.apply(pos + 1)))
                                .addAll(ann.getTypeArgs())
                                .build(),
                        ann.getTypeKwargs());
            }
            else {
                return buildSpec(annotatedType.getItem());
            }
        }, 0);
    }

    public static String buildSpec(TypeLike type)
    {
        return buildSpec(
                type.getName(),
                type.getTypeArgs(),
                type.getTypeKwargs());
    }

    public static String buildSpec(
            String name,
            List<Object> args,
            Map<String, Object> kwargs)
    {
        if (args.isEmpty() && kwargs.isEmpty()) {
            return name;
        }

        List<String> parts = Streams.concat(
                args.stream().map(TypeRendering::buildSpec),
                kwargs.entrySet().stream().map(e -> e.getKey() + "=" + buildSpec(e.getValue()))
        ).collect(toImmutableList());

        return name + '<' + Joiner.on(", ").join(parts) + '>';
    }
}
