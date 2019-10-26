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
package com.wrmsr.tokamak.core.layout.field.annotation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.tokamak.core.util.annotation.Annotation;

import javax.annotation.concurrent.Immutable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EphemeralField.class, name = "ephemeral"),
        @JsonSubTypes.Type(value = IdField.class, name = "id"),
        @JsonSubTypes.Type(value = ImmutableField.class, name = "immutable"),
        @JsonSubTypes.Type(value = InternalField.class, name = "internal"),
        @JsonSubTypes.Type(value = MonotonicField.class, name = "monotonic"),
        @JsonSubTypes.Type(value = SealedField.class, name = "sealed"),
        @JsonSubTypes.Type(value = UniqueField.class, name = "unique"),
})
@Immutable
public interface FieldAnnotation
        extends Annotation
{
    default boolean isTransitive()
    {
        return false;
    }

    static EphemeralField ephemeral()
    {
        return EphemeralField.INSTANCE;
    }

    static IdField id()
    {
        return IdField.INSTANCE;
    }

    static ImmutableField immutable()
    {
        return ImmutableField.INSTANCE;
    }

    static InternalField internal()
    {
        return InternalField.INSTANCE;
    }

    static MonotonicField monotonic()
    {
        return MonotonicField.INSTANCE;
    }

    static SealedField sealed()
    {
        return SealedField.INSTANCE;
    }

    static UniqueField unique()
    {
        return UniqueField.INSTANCE;
    }
}
