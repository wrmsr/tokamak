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

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class Types
{
    /*
    TODO:
     - coerce, check
     - serdes
     - sql:
     - presto:
      - json - jsr353? over jackson/glassfish? jackson JsonObjects? reparse? ..all..
       - JsonExtract.java, JsonPathType, JsonParser walking
       - https://github.com/json-path/JsonPath
      - interval
      - digest
      - ip
     - hppc
     - pluggability
    */

    private Types()
    {
    }

    public static final SimpleType<Void> VOID = new SimpleType<>("Void", Void.class);
    public static final SimpleType<Object> OBJECT = new SimpleType<>("Object", Object.class);

    public static final PrimitiveType<Boolean> BOOLEAN = new PrimitiveType<>("Boolean", Boolean.class, boolean.class, 1);
    public static final PrimitiveType<Long> LONG = new PrimitiveType<>("Long", Long.class, long.class, 8);
    public static final PrimitiveType<Double> DOUBLE = new PrimitiveType<>("Double", Double.class, double.class, 8);

    public static final SimpleType<String> STRING = new SimpleType<>("String", String.class);
    public static final SimpleType<byte[]> BYTES = new SimpleType<>("Bytes", byte[].class);

    public static final SimpleType<BigInteger> BIG_INTEGER = new SimpleType<>("BigInteger", BigInteger.class);
    public static final SimpleType<BigDecimal> BIG_DECIMAL = new SimpleType<>("BigDecimal", BigDecimal.class);

    public static final SimpleType<LocalDate> LOCAL_DATE = new SimpleType<>("LocalDate", LocalDate.class);
    public static final SimpleType<LocalTime> LOCAL_TIME = new SimpleType<>("LocalTime", LocalTime.class);
    public static final SimpleType<LocalDateTime> LOCAL_DATE_TIME = new SimpleType<>("LocalDateTime", LocalDateTime.class);
    public static final SimpleType<ZonedDateTime> ZONED_DATE_TIME = new SimpleType<>("ZonedDateTime", ZonedDateTime.class);
    public static final SimpleType<Duration> DURATION = new SimpleType<>("Duration", Duration.class);

    public static final Map<Class<?>, Type> FROM_JAVA_TYPE = ImmutableMap.<Class<?>, Type>builder()
            .put(Boolean.class, BOOLEAN)
            .put(boolean.class, BOOLEAN)
            .put(Long.class, LONG)
            .put(long.class, LONG)
            .put(Double.class, DOUBLE)
            .put(double.class, DOUBLE)
            .put(String.class, STRING)
            .put(byte[].class, BYTES)
            .build();

    public static final BiMap<Type, String> PRIMITIVE_STRING_MAP = ImmutableBiMap.<Type, String>builder()
            .put(BOOLEAN, "Boolean")
            .put(LONG, "Long")
            .put(DOUBLE, "Double")
            .put(BYTES, "Bytes")
            .put(STRING, "String")
            .build();

    public static Type parseRepr(String str)
    {
        return checkNotNull(PRIMITIVE_STRING_MAP.inverse().get(str));
    }

    public static String toRepr(Type type)
    {
        return PRIMITIVE_STRING_MAP.get(type);
    }

    public static Type fromJavaType(Class<?> cls)
    {
        return checkNotNull(FROM_JAVA_TYPE.get(cls));
    }

    static String buildArgsSpec(String name, List<Object> args)
    {
        return name + '<' + Joiner.on(", ").join(
                args.stream().map(v -> {
                    if (v instanceof Type) {
                        return ((Type) v).toSpec();
                    }
                    else if (v instanceof Long) {
                        return Long.toString((Long) v);
                    }
                    else {
                        throw new IllegalStateException(Objects.toString(v));
                    }
                }).collect(toImmutableList())) + '>';
    }

    static String buildKwargsSpec(String name, Map<String, Object> kwargs)
    {
        return name + '<' + Joiner.on(", ").join(
                kwargs.entrySet().stream().map(e -> {
                    Object v = e.getValue();
                    String vs;
                    if (v instanceof Type) {
                        vs = ((Type) v).toSpec();
                    }
                    else if (v instanceof Long) {
                        vs = Long.toString((Long) v);
                    }
                    else {
                        throw new IllegalStateException(Objects.toString(v));
                    }
                    return e.getKey() + '=' + vs;
                }).collect(toImmutableList())) + '>';
    }
}
