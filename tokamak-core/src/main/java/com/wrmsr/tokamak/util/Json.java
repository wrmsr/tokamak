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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.io.IOException;

public final class Json
{
    private Json()
    {
    }

    public static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = new ObjectMapper();

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
        objectMapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        objectMapper.disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        objectMapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);

        objectMapper.registerModules(
                new Jdk8Module(),
                new GuavaModule(),
                new JavaTimeModule());

        return objectMapper;
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

    public static <T> T roundTrip(ObjectMapper mapper, Object value, TypeReference<T> valueType)
    {
        try {
            return mapper.readValue(mapper.writeValueAsBytes(value), valueType);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object roundTrip(ObjectMapper mapper, Object value)
    {
        return roundTrip(mapper, value, new TypeReference<Object>() {});
    }

    public static <T> T roundTrip(Object value, TypeReference<T> valueType)
    {
        return roundTrip(OBJECT_MAPPER_SUPPLIER.get(), value, valueType);
    }

    public static Object roundTrip(Object value)
    {
        return roundTrip(value, new TypeReference<Object>() {});
    }
}
