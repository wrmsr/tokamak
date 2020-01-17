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

import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

@Immutable
public final class SetType
        extends ItemType
{
    public static final String NAME = "Set";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, SetType.class, Set.class, TypeConstructor.of(SetType::new));

    public SetType(Type itemType)
    {
        super(NAME, itemType);
    }

    @Override
    public java.lang.reflect.Type toReflect()
    {
        return Set.class;
    }
}
