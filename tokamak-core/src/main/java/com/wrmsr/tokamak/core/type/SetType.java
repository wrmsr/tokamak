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

import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class SetType
        extends AbstractType
{
    private final Type itemType;

    public SetType(Type itemType)
    {
        super("Set");
        this.itemType = checkNotNull(itemType);
    }

    @Override
    public String toString()
    {
        return "SetType{" +
                "itemType=" + itemType +
                '}';
    }

    public Type getItemType()
    {
        return itemType;
    }

    @Override
    public java.lang.reflect.Type getReflect()
    {
        return Set.class;
    }

    @Override
    public String toRepr()
    {
        return Types.buildArgsRepr(baseName, ImmutableList.of(itemType));
    }
}
