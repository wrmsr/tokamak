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
package com.wrmsr.tokamak.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

public final class Json
{
    /*
    mapper.configOverride(Object.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.ARRAY));
    */
    private static final Logger log = Logger.get(Json.class);

    private Json()
    {
    }

    public static ObjectMapper configureObjectMapper(ObjectMapper objectMapper)
    {
        // ignore unknown fields (for backwards compatibility)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // use ISO dates
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // skip fields that are null instead of writing an explicit json null value
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // disable auto detection of json properties... all properties must be explicit
        objectMapper.disable(MapperFeature.AUTO_DETECT_CREATORS);
        objectMapper.disable(MapperFeature.AUTO_DETECT_FIELDS);
        objectMapper.disable(MapperFeature.AUTO_DETECT_SETTERS);
        objectMapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        objectMapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
        objectMapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        objectMapper.disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        objectMapper.disable(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES);
        objectMapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
        objectMapper.disable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL);

        objectMapper.registerModules(
                new Jdk8Module(),
                new GuavaModule(),
                new JavaTimeModule());

        getAfterburnerModuleFactory().ifPresent(f -> objectMapper.registerModule(f.get()));

        return objectMapper;
    }

    private static final SupplierLazyValue<Optional<Class<? extends Module>>> afterburnerModule = new SupplierLazyValue<>();

    public static Optional<Class<? extends Module>> getAfterburnerModule()
    {
        return afterburnerModule.get(() -> {
            try {
                @SuppressWarnings({"unchecked"})
                Class<? extends Module> module = (Class<? extends Module>)
                        Class.forName("com.fasterxml.jackson.module.afterburner.AfterburnerModule");
                if (module != null) {
                    log.debug("Loaded afterburner");
                    return Optional.of(module);
                }
            }
            catch (Exception e) {
                log.debug("Failed to load afterburner: %s", e);
            }
            return Optional.empty();
        });
    }

    private static final SupplierLazyValue<Optional<Supplier<? extends Module>>> afterburnerModuleFactory = new SupplierLazyValue<>();

    public static Optional<Supplier<? extends Module>> getAfterburnerModuleFactory()
    {
        return afterburnerModuleFactory.get(() -> {
            Optional<Class<? extends Module>> cls = getAfterburnerModule();
            if (cls.isPresent()) {
                try {
                    Constructor<? extends Module> ctor = cls.get().getDeclaredConstructor();
                    if (ctor != null) {
                        Supplier<? extends Module> supplier = () -> {
                            try {
                                return ctor.newInstance();
                            }
                            catch (ReflectiveOperationException e) {
                                throw new RuntimeException(e);
                            }
                        };
                        Module module = supplier.get();
                        if (module != null) {
                            return Optional.of(supplier);
                        }
                    }
                }
                catch (Exception e) {
                    log.debug("Failed to create afterburner factory: %s", e);
                }
            }
            return Optional.empty();
        });
    }

    public static final Set<JsonParser.Feature> RELAXED_JSON_PARSER_FEATURES = ImmutableSet.of(
            JsonParser.Feature.ALLOW_COMMENTS,
            JsonParser.Feature.ALLOW_YAML_COMMENTS,
            JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
            JsonParser.Feature.ALLOW_SINGLE_QUOTES,
            JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
            JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
            JsonParser.Feature.ALLOW_MISSING_VALUES
    );

    public static JsonFactory configureRelaxedJsonFactory(JsonFactory jsonFactory)
    {
        RELAXED_JSON_PARSER_FEATURES.forEach(jsonFactory::enable);
        return jsonFactory;
    }

    public static ObjectMapper newObjectMapper()
    {
        return configureObjectMapper(new ObjectMapper());
    }

    public static ObjectMapper newRelaxedObjectMapper()
    {
        return configureObjectMapper(new ObjectMapper(configureRelaxedJsonFactory(new JsonFactory())));
    }

    public static ObjectMapper newCborObjectMapper()
    {
        return configureObjectMapper(new ObjectMapper(new CBORFactory()));
    }

    public static ObjectMapper newSmileObjectMapper()
    {
        return configureObjectMapper(new ObjectMapper(new SmileFactory()));
    }

    public static final Supplier<ObjectMapper> OBJECT_MAPPER_SUPPLIER = Suppliers.memoize(Json::newObjectMapper);

    public static String writeValue(Object object)
    {
        try {
            return OBJECT_MAPPER_SUPPLIER.get().writeValueAsString(object);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeValuePretty(Object object)
    {
        try {
            return OBJECT_MAPPER_SUPPLIER.get().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String src, Class<T> valueType)
    {
        try {
            return OBJECT_MAPPER_SUPPLIER.get().readValue(src, valueType);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String src, TypeReference<T> valueType)
    {
        try {
            return OBJECT_MAPPER_SUPPLIER.get().readValue(src, valueType);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T roundTripTree(ObjectMapper mapper, Object value, TypeReference<T> valueType)
    {
        try {
            TreeNode treeNode = mapper.valueToTree(value);
            JsonParser jsonParser = mapper.treeAsTokens(treeNode);
            return mapper.readValue(jsonParser, valueType);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object roundTripTree(ObjectMapper mapper, Object value)
    {
        return roundTripTree(mapper, value, new TypeReference<Object>() {});
    }

    public static <T> T roundTripTree(Object value, TypeReference<T> valueType)
    {
        return roundTripTree(OBJECT_MAPPER_SUPPLIER.get(), value, valueType);
    }

    public static Object roundTripTree(Object value)
    {
        return roundTripTree(value, new TypeReference<Object>() {});
    }

    public static <T> T roundTripBytes(ObjectMapper mapper, Object value, TypeReference<T> valueType)
    {
        try {
            return mapper.readValue(mapper.writeValueAsBytes(value), valueType);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object roundTripBytes(ObjectMapper mapper, Object value)
    {
        return roundTripBytes(mapper, value, new TypeReference<Object>() {});
    }

    public static <T> T roundTripBytes(Object value, TypeReference<T> valueType)
    {
        return roundTripBytes(OBJECT_MAPPER_SUPPLIER.get(), value, valueType);
    }

    public static Object roundTripBytes(Object value)
    {
        return roundTripBytes(value, new TypeReference<Object>() {});
    }

    public static <V> TypeReference<V> typeReference(Class<V> cls)
    {
        return new TypeReference<V>()
        {
            @Override
            public Type getType()
            {
                return cls;
            }
        };
    }
}
