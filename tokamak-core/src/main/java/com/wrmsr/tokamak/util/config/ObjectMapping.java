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
package com.wrmsr.tokamak.util.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSubclass;

final class ObjectMapping
{
    private ObjectMapping()
    {
    }

    public static class Serializer
            extends JsonSerializer<Config>
    {
        @Override
        public void serialize(Config value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException
        {
            gen.writeStartObject();
            ConfigMetadata md = Configs.getMetadata(value.getClass());
            // for (Map.Entry<String, Property> e : md.getProperties().entrySet()) {
            //     Object pv = e.getValue().get(value);
            //     if (pv != null) {
            //         gen.writeObjectField(e.getKey(), pv);
            //     }
            // }
            gen.writeEndObject();
        }
    }

    public static class Deserializer
            extends JsonDeserializer<Config>
            implements ContextualDeserializer
    {
        private JavaType type;
        private Class<? extends Config> cls;
        private ConfigMetadata metadata;

        public Deserializer()
        {
        }

        public Deserializer(JavaType type)
        {
            this.type = checkNotNull(type);
            cls = checkSubclass(type.getRawClass(), Config.class);
            metadata = Configs.getMetadata(cls);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctx, com.fasterxml.jackson.databind.BeanProperty property)
                throws JsonMappingException
        {
            return new Deserializer(property != null ? property.getType() : ctx.getContextualType());
        }

        @Override
        public Config deserialize(JsonParser parser, DeserializationContext ctx)
                throws IOException, JsonProcessingException
        {
            checkNotNull(metadata);
            Config cfg;
            try {
                cfg = cls.getDeclaredConstructor().newInstance();
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            checkState(parser.currentToken() == JsonToken.START_OBJECT);
            ObjectCodec codec = parser.getCodec();
            // while (parser.nextToken() != JsonToken.END_OBJECT) {
            //     checkState(parser.currentToken() == JsonToken.FIELD_NAME);
            //     String name = parser.getValueAsString();
            //     Property prop = checkNotNull(metadata.getProperties().get(name));
            //     parser.nextToken();
            //     Object value = codec.readValue(codec.treeAsTokens(parser.readValueAsTree()), prop.getType());
            //     prop.set(cfg, value);
            // }
            return cfg;
        }
    }
}
