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
package com.wrmsr.tokamak.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.wrmsr.tokamak.util.codec.Codec;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CodecSerialization<F, T>
{
    private final Codec<F, T> codec;
    private final Class<F> decodedCls;
    private final Class<T> encodedCls;

    private final JsonSerializer<F> serializer;
    private final JsonDeserializer<F> deserializer;

    public CodecSerialization(Class<F> decodedCls, Class<T> encodedCls, Codec<F, T> codec)
    {
        this.codec = checkNotNull(codec);
        this.decodedCls = checkNotNull(decodedCls);
        this.encodedCls = checkNotNull(encodedCls);

        serializer = new JsonSerializer<F>()
        {
            @Override
            public void serialize(F value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException
            {
                T encoded = codec.encode(value);
                gen.writeObject(encoded);
            }
        };

        deserializer = new JsonDeserializer<F>()
        {
            @Override
            public F deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException
            {
                T encoded = p.readValueAs(encodedCls);
                return codec.decode(encoded);
            }
        };
    }

    public Codec<F, T> getCodec()
    {
        return codec;
    }

    public Class<F> getDecodedCls()
    {
        return decodedCls;
    }

    public Class<T> getEncodedCls()
    {
        return encodedCls;
    }

    public JsonSerializer<F> getSerializer()
    {
        return serializer;
    }

    public JsonDeserializer<F> getDeserializer()
    {
        return deserializer;
    }

    public SimpleModule install(SimpleModule module)
    {
        module.addSerializer(decodedCls, serializer);
        module.addDeserializer(decodedCls, deserializer);
        return module;
    }

    public Module toModule()
    {
        return install(new SimpleModule());
    }

    public ObjectMapper install(ObjectMapper mapper)
    {
        mapper.registerModule(toModule());
        return mapper;
    }
}
