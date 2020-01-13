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
import com.wrmsr.tokamak.core.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public abstract class KeyValueType
        extends AbstractType
{
    public KeyValueType(String baseName, OptionalInt fixedSize, Type key, Type value)
    {
        super(baseName, fixedSize, ImmutableList.of(key, value), ImmutableMap.of());
    }

    public KeyValueType(String baseName, int fixedSize, Type key, Type value)
    {
        this(baseName, OptionalInt.of(fixedSize), key, value);
    }

    public KeyValueType(String baseName, Type key, Type value)
    {
        this(baseName, OptionalInt.empty(), key, value);
    }

    public Type getKey()
    {
        return (Type) checkNotNull(getArgs().get(0));
    }

    public Type getValue()
    {
        return (Type) checkNotNull(getArgs().get(1));
    }
}
