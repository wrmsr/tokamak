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

import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.type.hier.special.AnnotatedType;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

public final class TypeAnnotations
{
    private TypeAnnotations()
    {
    }

    public static Type strip(Type type)
    {
        if (type instanceof AnnotatedType) {
            Type stripped = ((AnnotatedType) type).getItem();
            checkState(!(stripped instanceof AnnotatedType));
            return stripped;
        }
        else {
            return type;
        }
    }

    public static AnnotationCollection<TypeAnnotation> from(Type type)
    {
        if (type instanceof AnnotatedType) {
            return ((AnnotatedType) type).getAnnotations();
        }
        else {
            return AnnotationCollection.of();
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends TypeAnnotation> Optional<T> get(Type type, Class<T> cls)
    {
        return (Optional<T>) from(type).get(cls);
    }

    public static <T extends TypeAnnotation> boolean has(Type type, Class<T> cls)
    {
        return get(type, cls).isPresent();
    }

    public static Type set(Type type, Iterable<TypeAnnotation> anns)
    {
        return AnnotatedType.of(anns, strip(type));
    }

    public static Type map(Type type, Function<AnnotationCollection<TypeAnnotation>, AnnotationCollection<TypeAnnotation>> fn)
    {
        return set(type, fn.apply(from(type)));
    }
}
