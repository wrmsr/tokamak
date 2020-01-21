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
package com.wrmsr.tokamak.core.type.annotation;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.AbstractTypeLike;
import com.wrmsr.tokamak.core.type.TypeAnnotation;
import com.wrmsr.tokamak.core.type.TypeRendering;

import javax.annotation.concurrent.Immutable;

import java.util.List;

@Immutable
public abstract class AbstractTypeAnnotation
        extends AbstractTypeLike
        implements TypeAnnotation
{
    public AbstractTypeAnnotation(
            List<Object> args,
            ImmutableMap<String, Object> kwargs)
    {
        super(args, kwargs);
    }

    public AbstractTypeAnnotation()
    {
        this(ImmutableList.of(), ImmutableMap.of());
    }

    @Override
    @JsonValue
    public final String toSpec()
    {
        return TypeRendering.buildSpec(getName(), args, kwargs);
    }
}
