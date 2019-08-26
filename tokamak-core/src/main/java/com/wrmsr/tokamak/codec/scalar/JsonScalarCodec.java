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
package com.wrmsr.tokamak.codec.scalar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;
import com.wrmsr.tokamak.util.Json;

import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JsonScalarCodec<V>
        implements ScalarCodec<V>
{
    private final TypeReference<V> typeReference;
    private final ObjectMapper objectMapper;

    public JsonScalarCodec(TypeReference<V> typeReference, ObjectMapper objectMapper)
    {
        this.typeReference = checkNotNull(typeReference);
        this.objectMapper = checkNotNull(objectMapper);
    }

    public JsonScalarCodec(Class<V> cls, ObjectMapper objectMapper)
    {
        this(new TypeReference<V>()
        {
            @Override
            public Type getType()
            {
                return cls;
            }
        }, objectMapper);
    }

    public JsonScalarCodec(TypeReference<V> typeReference)
    {
        this(typeReference, Json.OBJECT_MAPPER_SUPPLIER.get());
    }

    public JsonScalarCodec(Class<V> cls)
    {
        this(cls, Json.OBJECT_MAPPER_SUPPLIER.get());
    }

    public TypeReference<V> getTypeReference()
    {
        return typeReference;
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    @Override
    public void encode(V value, Output output)
    {
        try {
            objectMapper.writeValue(output.toOutputStream(), value);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public V decode(Input input)
    {
        try {
            return objectMapper.readValue(input.getBytes(), typeReference);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
