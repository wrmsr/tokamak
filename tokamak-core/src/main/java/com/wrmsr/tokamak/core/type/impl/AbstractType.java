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

import com.fasterxml.jackson.annotation.JsonValue;
import com.wrmsr.tokamak.core.type.AbstractTypeLike;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.TypeAnnotation;
import com.wrmsr.tokamak.core.type.TypeRendering;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

@Immutable
public abstract class AbstractType
        extends AbstractTypeLike
        implements Type
{
    private final AnnotationCollection<TypeAnnotation> annotations;

    public AbstractType(
            List<Object> args,
            Map<String, Object> kwargs,
            Iterable<TypeAnnotation> annotations)
    {
        super(args, kwargs);

        this.annotations = AnnotationCollection.copyOf(annotations);
    }

    @Override
    public AnnotationCollection<TypeAnnotation> getAnnotations()
    {
        return annotations;
    }

    @Override
    @JsonValue
    public final String toSpec()
    {
        return TypeRendering.buildSpec(getName(), args, kwargs);
    }
}
