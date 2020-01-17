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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.TypeConstructor;
import com.wrmsr.tokamak.core.type.TypeRegistration;

import javax.annotation.concurrent.Immutable;

import java.util.Comparator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

@Immutable
public final class EnumType
        extends AbstractType
{
    public static final String NAME = "Enum";
    public static final TypeRegistration REGISTRATION = new TypeRegistration(NAME, EnumType.class, Enum.class, TypeConstructor.of(
            (Map<String, Object> kwargs) -> new EnumType(immutableMapValues(kwargs, Long.class::cast))));

    private static Map<String, Long> sortValues(Map<String, Long> values)
    {
        return values.entrySet().stream().sorted(Comparator.comparingLong(e -> checkNotNull(e.getValue()))).collect(toImmutableMap());
    }

    public EnumType(Map<String, Long> values)
    {
        super(NAME, ImmutableMap.copyOf(sortValues(values)));
    }

    @Override
    public java.lang.reflect.Type toReflect()
    {
        return Enum.class;
    }
}
