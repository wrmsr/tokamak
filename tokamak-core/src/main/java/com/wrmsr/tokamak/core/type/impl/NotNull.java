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
import com.wrmsr.tokamak.core.type.Types;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class NotNull
        extends AbstractType
{
    private final Type item;

    public NotNull(Type item)
    {
        super("NotNull");
        this.item = checkNotNull(item);
    }

    public Type getItem()
    {
        return item;
    }

    @Override
    public java.lang.reflect.Type getReflect()
    {
        return item.getReflect();
    }

    @Override
    public String toSpec()
    {
        return Types.buildArgsSpec(baseName, ImmutableList.of(item));
    }
}