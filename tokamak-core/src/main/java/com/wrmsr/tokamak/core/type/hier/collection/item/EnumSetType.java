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
package com.wrmsr.tokamak.core.type.hier.collection.item;

import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.special.EnumType;

import javax.annotation.concurrent.Immutable;

import java.util.EnumSet;
import java.util.Optional;

@Immutable
public final class EnumSetType
        extends AbstractItemType
{
    public static final String NAME = "EnumSet";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, EnumSetType.class, EnumSet.class, TypeConstructor.of(
            (Type enumType) -> new EnumSetType((EnumType) enumType)));

    public EnumSetType(EnumType item)
    {
        super(item);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public EnumType getItem()
    {
        return (EnumType) super.getItem();
    }

    @Override
    public Optional<java.lang.reflect.Type> toReflect()
    {
        return Optional.of(EnumSet.class);
    }
}
