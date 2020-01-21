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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.type.hier.TypeLike;
import com.wrmsr.tokamak.core.type.hier.special.AnnotatedType;
import com.wrmsr.tokamak.core.type.hier.special.UnknownType;
import com.wrmsr.tokamak.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
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
    private volatile Map<Class<? extends TypeLike>, TypeRegistration> registrationsByCls = ImmutableMap.of();
    private volatile Map<java.lang.reflect.Type, TypeRegistration> registrationsByReflect = ImmutableMap.of();

    public Map<String, TypeRegistration> getRegistrationsByName()
    {
        return registrationsByName;
    }

    public Map<Class<? extends TypeLike>, TypeRegistration> getRegistrationsByCls()
    {
        return registrationsByCls;
    }

    public Map<java.lang.reflect.Type, TypeRegistration> getRegistrationsByReflect()
    {
        return registrationsByReflect;
    }

    public TypeRegistration register(TypeRegistration registration)
    {
        synchronized (lock) {
            if (registrationsByName.containsKey(registration.getName())) {
                throw new IllegalArgumentException(String.format("Type name %s taken", registration.getName()));
            }
            if (registrationsByCls.containsKey(registration.getCls())) {
                throw new IllegalArgumentException(String.format("Type class %s taken", registration.getCls()));
            }
            checkArgument(!Types.INTERNAL_TYPE_INTERFACES.contains(registration.getCls()));
            if (registration.getReflect().isPresent() && registrationsByReflect.containsKey(registration.getReflect().get())) {
                throw new IllegalArgumentException(String.format("Type reflect %s taken", registration.getReflect().get()));
            }

            registrationsByName = ImmutableMap.<String, TypeRegistration>builder()
                    .putAll(registrationsByName)
                    .put(registration.getName(), registration)
                    .build();
            registrationsByCls = ImmutableMap.<Class<? extends TypeLike>, TypeRegistration>builder()
                    .putAll(registrationsByCls)
                    .put(registration.getCls(), registration)
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
        return (Type) fromParsed(parsedType);
    }

    private Object fromParsed(Object item)
    {
        if (item instanceof TypeParsing.ParsedType) {
            TypeParsing.ParsedType parsedType = (TypeParsing.ParsedType) item;
            TypeRegistration registration = registrationsByName.get(parsedType.getName());
            List<Object> args = parsedType.getArgs();
            Map<String, Object> kwargs = parsedType.getKwargs();

            if (TypeAnnotation.class.isAssignableFrom(registration.getCls())) {
                checkArgument(!args.isEmpty());
                TypeRegistration annotatedReg = checkNotNull(registrationsByCls.get(AnnotatedType.class));

                TypeAnnotation ann = (TypeAnnotation) registration.construct(
                        immutableMapItems(args.subList(1, args.size()), this::fromParsed),
                        immutableMapValues(kwargs, this::fromParsed));
                Type annItem = (Type) fromParsed(args.get(0));

                return annotatedReg.construct(
                        ImmutableList.builder().add(ann).add(annItem).build(),
                        ImmutableMap.of());
            }

            else {
                return registration.construct(
                        immutableMapItems(args, this::fromParsed),
                        immutableMapValues(kwargs, this::fromParsed));
            }
        }

        else {
            return item;
        }
    }

    private final class Deserializer<T extends Type>
            extends JsonDeserializer<T>
    {
        private final Class<T> cls;

        public Deserializer(Class<T> cls)
        {
            this.cls = checkNotNull(cls);
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException
        {
            String spec = p.readValueAs(String.class);
            return cls.cast(fromSpec(spec));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ObjectMapper registerDeserializers(ObjectMapper objectMapper)
    {
        SimpleModule module = new SimpleModule();
        for (Class<? extends TypeLike> cls : Types.INTERNAL_TYPE_INTERFACES) {
            module.addDeserializer(cls, new Deserializer(cls));
        }
        for (Class<? extends TypeLike> cls : registrationsByCls.keySet()) {
            module.addDeserializer(cls, new Deserializer(cls));
        }
        objectMapper.registerModule(module);
        return objectMapper;
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
