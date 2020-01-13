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
import com.wrmsr.tokamak.util.Pair;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeRegistry
{
    /*
    TODO:
     - inheritance
    */

    private final Object lock = new Object();

    private volatile ImmutableMap<String, TypeRegistrant> registrantsByBaseName = ImmutableMap.of();
    private volatile ImmutableMap<java.lang.reflect.Type, TypeRegistrant> registrantsByReflect = ImmutableMap.of();

    public ImmutableMap<String, TypeRegistrant> getRegistrantsByBaseName()
    {
        return registrantsByBaseName;
    }

    public ImmutableMap<java.lang.reflect.Type, TypeRegistrant> getRegistrantsByReflect()
    {
        return registrantsByReflect;
    }

    public TypeRegistrant register(TypeRegistrant registrant)
    {
        synchronized (lock) {
            if (registrantsByBaseName.containsKey(registrant.getBaseName())) {
                throw new IllegalArgumentException(String.format("Type base name %s taken", registrant.getBaseName()));
            }
            if (registrant.getReflect().isPresent() && registrantsByReflect.containsKey(registrant.getReflect().get())) {
                throw new IllegalArgumentException(String.format("Type reflect name %s taken", registrant.getReflect().get()));
            }

            registrantsByBaseName = ImmutableMap.<String, TypeRegistrant>builder()
                    .putAll(registrantsByBaseName)
                    .put(registrant.getBaseName(), registrant)
                    .build();
            registrantsByReflect = ImmutableMap.<java.lang.reflect.Type, TypeRegistrant>builder()
                    .putAll(registrantsByReflect)
                    .putAll(registrant.getReflect().isPresent() ? ImmutableList.of(Pair.immutable(registrant.getReflect().get(), registrant)) : ImmutableList.of())
                    .build();
        }
        return registrant;
    }

    public Type fromSpec(String str)
    {
        throw new IllegalStateException();
    }

    public Type fromReflect(java.lang.reflect.Type reflect)
    {
        return registrantsByReflect.get(reflect).getConstructor().construct(ImmutableList.of(), ImmutableMap.of());
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
