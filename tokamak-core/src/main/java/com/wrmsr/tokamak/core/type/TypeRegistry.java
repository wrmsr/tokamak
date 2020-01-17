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
import com.wrmsr.tokamak.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;

public final class TypeRegistry
{
    /*
    TODO:
     - inheritance
    */

    private final Object lock = new Object();

    private volatile Map<String, TypeRegistration> registrationsByBaseName = ImmutableMap.of();
    private volatile Map<java.lang.reflect.Type, TypeRegistration> registrationsByReflect = ImmutableMap.of();

    public Map<String, TypeRegistration> getRegistrationsByBaseName()
    {
        return registrationsByBaseName;
    }

    public Map<java.lang.reflect.Type, TypeRegistration> getRegistrationsByReflect()
    {
        return registrationsByReflect;
    }

    public TypeRegistration register(TypeRegistration registration)
    {
        synchronized (lock) {
            if (registrationsByBaseName.containsKey(registration.getBaseName())) {
                throw new IllegalArgumentException(String.format("Type base name %s taken", registration.getBaseName()));
            }
            if (registration.getReflect().isPresent() && registrationsByReflect.containsKey(registration.getReflect().get())) {
                throw new IllegalArgumentException(String.format("Type reflect %s taken", registration.getReflect().get()));
            }

            registrationsByBaseName = ImmutableMap.<String, TypeRegistration>builder()
                    .putAll(registrationsByBaseName)
                    .put(registration.getBaseName(), registration)
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
        NormalizedType normalizedType = new NormalizedType(type);
        return (Type) denormalize(normalizedType);
    }

    private Object fromParsed(Object item)
    {
        if (item instanceof TypeParsing.ParsedType) {
            TypeParsing.ParsedType parsedType = (TypeParsing.ParsedType) item;
            TypeRegistration registration = registrationsByBaseName.get(parsedType.getName());
            return registration.construct(
                    immutableMapItems(parsedType.getArgs(), this::fromParsed),
                    immutableMapValues(parsedType.getKwargs(), this::fromParsed));
        }
        else {
            return item;
        }
    }

    public Object denormalize(Object item)
    {
        if (item instanceof NormalizedType) {
            NormalizedType normalizedType = (NormalizedType) item;
            TypeRegistration registration = registrationsByBaseName.get(normalizedType.getItem().getBaseName());
            Type type = registration.construct(
                    immutableMapItems(normalizedType.getArgs(), this::denormalize),
                    immutableMapValues(normalizedType.getKwargs(), this::denormalize));

            List<Type.Sigil> sigils = newArrayList(normalizedType.getSigilsByName().values());
            sigils.sort(Comparator.comparing(Type::getBaseName, Ordering.natural()).reversed());
            for (Type.Sigil sigil : sigils) {
                TypeRegistration sigilRegistration = registrationsByBaseName.get(sigil.getBaseName());
                type = sigilRegistration.construct(
                        ImmutableList.builder()
                                .add(type)
                                .addAll(immutableMapItems(sigil.getArgs().subList(1, sigil.getArgs().size()), this::denormalize))
                                .build(),
                        immutableMapValues(sigil.getKwargs(), this::denormalize));
            }

            return type;
        }

        else if (item instanceof Type) {
            throw new IllegalArgumentException(Objects.toString(item));
        }

        else {
            return item;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Type fromReflect(java.lang.reflect.Type reflect)
    {
        return registrationsByReflect.get(Primitives.wrap((Class) reflect)).getConstructor().construct(ImmutableList.of(), ImmutableMap.of());
    }

    public boolean areEquivalent(Type l, Type r)
    {
        checkNotNull(l);
        checkNotNull(r);
        if (l == Types.UNKNOWN || r == Types.UNKNOWN) {
            return true;
        }
        // FIXME: structural, nominal, lolinal
        return l.toSpec().equals(r.toSpec());
    }
}
