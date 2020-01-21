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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Primitives;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.special.UnknownType;
import com.wrmsr.tokamak.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;

public final class TypeRegistry
{
    /*
    TODO:
     - inheritance
    */

    private final Object lock = new Object();

    private volatile Map<String, TypeRegistration> registrationsByName = ImmutableMap.of();
    private volatile Map<java.lang.reflect.Type, TypeRegistration> registrationsByReflect = ImmutableMap.of();

    public Map<String, TypeRegistration> getRegistrationsByName()
    {
        return registrationsByName;
    }

    public Map<java.lang.reflect.Type, TypeRegistration> getRegistrationsByReflect()
    {
        return registrationsByReflect;
    }

    public TypeRegistration register(TypeRegistration registration)
    {
        synchronized (lock) {
            if (registrationsByName.containsKey(registration.getName())) {
                throw new IllegalArgumentException(String.format("Type base name %s taken", registration.getName()));
            }
            if (registration.getReflect().isPresent() && registrationsByReflect.containsKey(registration.getReflect().get())) {
                throw new IllegalArgumentException(String.format("Type reflect %s taken", registration.getReflect().get()));
            }

            registrationsByName = ImmutableMap.<String, TypeRegistration>builder()
                    .putAll(registrationsByName)
                    .put(registration.getName(), registration)
                    .build();
            registrationsByReflect = ImmutableMap.<java.lang.reflect.Type, TypeRegistration>builder()
                    .putAll(registrationsByReflect)
                    .putAll(registration.getReflect().isPresent() ? ImmutableList.of(Pair.immutable(registration.getReflect().get(), registration)) : ImmutableList.of())
                    .build();
        }
        return registration;
    }

    public Type fromSpec(String str)
    {
        TypeParsing.ParsedType parsedType = TypeParsing.parseType(str);
        Type type = (Type) fromParsed(parsedType);
        // DesigiledType desigiledType = new DesigiledType(type);
        // return (Type) resigil(desigiledType);
        return type;
    }

    private Object fromParsed(Object item)
    {
        if (item instanceof TypeParsing.ParsedType) {
            TypeParsing.ParsedType parsedType = (TypeParsing.ParsedType) item;
            TypeRegistration registration = registrationsByName.get(parsedType.getName());
            return registration.construct(
                    immutableMapItems(parsedType.getArgs(), this::fromParsed),
                    immutableMapValues(parsedType.getKwargs(), this::fromParsed));
        }
        else {
            return item;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Type fromReflect(java.lang.reflect.Type reflect)
    {
        return (Type) registrationsByReflect
                .get(Primitives.wrap((Class) reflect))
                .getConstructor()
                .construct(ImmutableList.of(), ImmutableMap.of());
    }

    public boolean areEquivalent(Type l, Type r)
    {
        checkNotNull(l);
        checkNotNull(r);
        if (l instanceof UnknownType || r instanceof UnknownType) {
            return true;
        }
        // FIXME: structural, nominal, lolinal
        return l.toSpec().equals(r.toSpec());
    }
}
