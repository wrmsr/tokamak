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
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSubclass;

public final class ConfigObjectMapping
{
    private ConfigObjectMapping()
    {
    }

    public static abstract class Property
    {
        private String name;

        public Property(String name)
        {
            this.name = name;
        }

        public abstract Object get(Config cfg);

        public abstract void set(Config cfg, Object value);
    }

    public static final class BeanProperty
            extends Property
    {
        private final Method setter;
        private final Optional<Method> getter;

        public BeanProperty(String name, Method setter, Optional<Method> getter)
        {
            super(name);
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        public Object get(Config cfg)
        {
            try {
                return getter.get().invoke(cfg);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void set(Config cfg, Object value)
        {
            try {
                setter.invoke(cfg, value);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class FieldProperty
            extends Property
    {
        private final Field field;

        public FieldProperty(String name, Field field)
        {
            super(name);
            this.field = field;
        }

        @Override
        public Object get(Config cfg)
        {
            try {
                return field.get(cfg);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void set(Config cfg, Object value)
        {
            try {
                field.set(cfg, value);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Map<String, Property> buildProperties(Class<? extends Config> cls)
    {
        ImmutableMap.Builder<String, Property> map = ImmutableMap.builder();
        for (Class<?> cur = cls; (cur != null) && !cur.equals(Object.class); cur = cur.getSuperclass()) {
            for (Method method : cur.getDeclaredMethods()) {
                if (method.isAnnotationPresent(ConfigProperty.class)) {
                    checkArgument(method.getName().startsWith("set"));
                    String name = method.getName().substring(3);
                    checkArgument(Character.isUpperCase(name.charAt(0)));
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    map.put(name, new BeanProperty(name, method, Optional.empty()));
                }
            }
            for (Field field : cur.getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    String name = field.getName();
                    map.put(name, new FieldProperty(name, field));
                }
            }
        }
        return map.build();
    }

    public static class Serializer
            extends JsonSerializer<Config>
    {
        @Override
        public void serialize(Config value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException
        {
            gen.writeStartObject();
            Map<String, Property> properties = buildProperties(value.getClass());
            for (Map.Entry<String, Property> e : properties.entrySet()) {
                Object pv = e.getValue().get(value);
                if (pv != null) {
                    gen.writeObjectField(e.getKey(), pv);
                }
            }
            gen.writeEndObject();
        }
    }

    public static class Deserializer
            extends JsonDeserializer<Config>
            implements ContextualDeserializer
    {
        private JavaType type;
        private Class<? extends Config> cls;
        private Map<String, Property> properties;

        public Deserializer()
        {
        }

        public Deserializer(JavaType type)
        {
            this.type = checkNotNull(type);
            cls = checkSubclass(type.getRawClass(), Config.class);
            properties = buildProperties(cls);
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
            checkNotNull(properties);
            Config cfg;
            try {
                cfg = cls.getDeclaredConstructor().newInstance();
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            checkState(parser.currentToken() == JsonToken.START_OBJECT);
            ObjectCodec codec = parser.getCodec();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                checkState(parser.currentToken() == JsonToken.FIELD_NAME);
                String name = parser.getValueAsString();
                Property prop = checkNotNull(properties.get(name));
                parser.nextToken();
                Object value = codec.readValue(codec.treeAsTokens(parser.readValueAsTree()), Object.class);
                prop.set(cfg, value);
            }
            return cfg;
        }
    }
}
