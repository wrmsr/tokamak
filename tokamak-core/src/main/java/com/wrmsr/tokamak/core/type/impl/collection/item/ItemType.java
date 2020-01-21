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
package com.wrmsr.tokamak.core.type.impl.collection.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.impl.AbstractType;

import javax.annotation.concurrent.Immutable;

import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public abstract class ItemType
        extends AbstractType
{
    public ItemType(String name, Type itemType, OptionalInt fixedSize)
    {
        super(name, fixedSize, ImmutableList.of(itemType), ImmutableMap.of());
    }

    public ItemType(String name, int fixedSize, Type itemType)
    {
        this(name, OptionalInt.of(fixedSize), itemType);
    }

    public ItemType(String name, Type itemType)
    {
        this(name, OptionalInt.empty(), itemType);
    }

    public Type getItem()
    {
        return (Type) checkNotNull(getArgs().get(0));
    }
}
