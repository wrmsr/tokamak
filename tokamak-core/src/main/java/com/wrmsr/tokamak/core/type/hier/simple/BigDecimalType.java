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
package com.wrmsr.tokamak.core.type.hier.simple;

import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;

import javax.annotation.concurrent.Immutable;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Optional;

@Immutable
public final class BigDecimalType
        implements SimpleType
{
    public static final String NAME = "BigDecimal";
    public static final BigDecimalType INSTANCE = new BigDecimalType();
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, BigDecimalType.class, BigDecimal.class, TypeConstructor.of(INSTANCE));

    public BigDecimalType()
    {
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public Optional<Type> toReflect()
    {
        return Optional.of(BigDecimal.class);
    }
}
