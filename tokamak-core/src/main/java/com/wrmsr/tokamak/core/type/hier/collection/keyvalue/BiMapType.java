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
package com.wrmsr.tokamak.core.type.hier.collection.keyvalue;

import com.google.common.collect.BiMap;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;
import com.wrmsr.tokamak.core.type.hier.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

@Immutable
public final class BiMapType
        extends AbstractKeyValueType
{
    public static final String NAME = "BiMap";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, BiMapType.class, BiMap.class, TypeConstructor.of(BiMapType::new));

    public BiMapType(Type keyType, Type valueType)
    {
        super(keyType, valueType);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public Optional<java.lang.reflect.Type> toReflect()
    {
        return Optional.of(BiMap.class);
    }
}
