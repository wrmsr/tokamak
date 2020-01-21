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
package com.wrmsr.tokamak.core.type.hier.primitive;

import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;

import javax.annotation.concurrent.Immutable;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalInt;

@Immutable
public final class LongType
        implements PrimitiveType
{
    public static final String NAME = "Long";
    public static final LongType INSTANCE = new LongType();
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, LongType.class, Long.class, TypeConstructor.of(INSTANCE));

    public LongType()
    {
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public OptionalInt getFixedSize()
    {
        return OptionalInt.of(8);
    }

    @Override
    public Optional<Type> toReflect()
    {
        return Optional.of(Long.class);
    }
}
