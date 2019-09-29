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
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.Logger;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public final class Json
{
    /*
    mapper.configOverride(Object.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.ARRAY));
    */
    private static final Logger log = Logger.get(Json.class);

    private Json()
    {
    }

    public static final Set<MapperFeature> DEFAULT_DISABLED_FEATURES = ImmutableSet.of(
            MapperFeature.AUTO_DETECT_CREATORS,
            MapperFeature.AUTO_DETECT_FIELDS,
            MapperFeature.AUTO_DETECT_SETTERS,
            MapperFeature.AUTO_DETECT_GETTERS,
            MapperFeature.AUTO_DETECT_IS_GETTERS,
            MapperFeature.USE_GETTERS_AS_SETTERS,
            MapperFeature.INFER_PROPERTY_MUTATORS,
            MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES,
            MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS,
            MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL
    );

    public static final List<Supplier<Module>> DEFAULT_MODULE_FACTORIES = new CopyOnWriteArrayList<>(ImmutableList.of(
            GuavaModule::new,
            JavaTimeModule::new,
            Jdk8Module::new
    ));

    public static ObjectMapper configureObjectMapper(ObjectMapper objectMapper)
    {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        DEFAULT_DISABLED_FEATURES.forEach(objectMapper::disable);
        DEFAULT_MODULE_FACTORIES.forEach(f -> objectMapper.registerModule(f.get()));
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

    public static final Set<JsonParser.Feature> RELAXED_JSON_PARSER_ENABLED_FEATURES = ImmutableSet.of(
            JsonParser.Feature.ALLOW_COMMENTS,
            JsonParser.Feature.ALLOW_YAML_COMMENTS,
            JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
            JsonParser.Feature.ALLOW_SINGLE_QUOTES
    );

    public static JsonFactory configureRelaxedJsonFactory(JsonFactory jsonFactory)
    {
        RELAXED_JSON_PARSER_ENABLED_FEATURES.forEach(jsonFactory::enable);
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
