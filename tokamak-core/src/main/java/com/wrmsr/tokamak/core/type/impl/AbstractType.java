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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.AbstractTypeLike;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.TypeAnnotation;
import com.wrmsr.tokamak.core.type.TypeRendering;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public abstract class AbstractType
        extends AbstractTypeLike
        implements Type
{
    protected final OptionalInt fixedSize;

    public AbstractType(
            String name,
            List<Object> args,
            Map<String, Object> kwargs,
            OptionalInt fixedSize)
    {
        super(name, args, kwargs);

        this.fixedSize = checkNotNull(fixedSize);
    }

    public AbstractType(String name, OptionalInt fixedSize)
    {
        this(name, ImmutableList.of(), ImmutableMap.of(), fixedSize);
    }

    public AbstractType(String name, int fixedSize)
    {
        this(name, OptionalInt.of(fixedSize));
    }

    public AbstractType(String name)
    {
        this(name, OptionalInt.empty());
    }

    public AbstractType(String name, List<Object> args)
    {
        this(name, args, ImmutableMap.of(), OptionalInt.empty());
    }

    public AbstractType(String name, Map<String, Object> kwargs)
    {
        this(name, ImmutableList.of(), kwargs, OptionalInt.empty());
    }

    @Override
    public OptionalInt getFixedSize()
    {
        return fixedSize;
    }

    @Override
    public final String toSpec()
    {
        return TypeRendering.buildSpec(name, args, kwargs);
    }
}
