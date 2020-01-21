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

import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;

import java.util.Optional;
import java.util.OptionalInt;

public interface Type
        extends TypeLike
{
    default AnnotationCollection<TypeAnnotation> getAnnotations()
    {
        return AnnotationCollection.of();
    }

    default OptionalInt getFixedSize()
    {
        return OptionalInt.empty();
    }

    default Optional<java.lang.reflect.Type> toReflect()
    {
        return Optional.empty();
    }
}
