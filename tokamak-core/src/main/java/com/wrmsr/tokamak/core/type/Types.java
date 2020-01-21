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

import com.google.common.base.Preconditions;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeLike;
import com.wrmsr.tokamak.core.type.hier.PrimitiveType;
import com.wrmsr.tokamak.core.type.hier.SimpleType;
import com.wrmsr.tokamak.core.type.hier.SpecialType;
import com.wrmsr.tokamak.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class Types
{
    /*
    com.google.common.primitives.Primitives

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
     - structural areEquivalent
    */

    private Types()
    {
    }

    /*
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

    public static final SpecialType UNKNOWN = new SpecialType("?");
    public static final SpecialType ARGS = new SpecialType("Args");
    public static final SpecialType JIT_FUNCTION = new SpecialType("JitFunction");

    public static final Set<Type> BUILTIN_TYPES = ImmutableSet.<Type>builder()

            .add(VOID)
            .add(OBJECT)

            .add(BOOLEAN)
            .add(LONG)
            .add(DOUBLE)

            .add(STRING)
            .add(BYTES)

            .add(BIG_INTEGER)
            .add(BIG_DECIMAL)

            .add(LOCAL_DATE)
            .add(LOCAL_TIME)
            .add(LOCAL_DATE_TIME)
            .add(ZONED_DATE_TIME)
            .add(DURATION)

            .add(UNKNOWN)
            .add(ARGS)
            .add(JIT_FUNCTION)

            .build();

    public static final List<TypeRegistration> BUILTIN_REGISTRATIONS = ImmutableList.<TypeRegistration>builder()

            .addAll(BUILTIN_TYPES.stream().map(TypeRegistration::standard).collect(toImmutableList()))

            .add(BiMapType.REGISTRATION)
            .add(EnumSetType.REGISTRATION)
            .add(EnumType.REGISTRATION)
            .add(FunctionType.REGISTRATION)
            .add(ListType.REGISTRATION)
            .add(MapType.REGISTRATION)
            .add(SetType.REGISTRATION)
            .add(StructType.REGISTRATION)
            .add(StructuralType.REGISTRATION)
            .add(TupleType.REGISTRATION)
            .add(UnionType.REGISTRATION)

            .add(InternalType.REGISTRATION)
            .add(NotNullType.REGISTRATION)
            .add(SizedType.REGISTRATION)

            .build();
     */

    public static final TypeRegistry BUILTIN_REGISTRY = ((Supplier<TypeRegistry>) () -> {
        TypeRegistry registry = new TypeRegistry();
        BUILTIN_REGISTRATIONS.forEach(registry::register);
        return registry;
    }).get();

    public static boolean isValidArg(Object a)
    {
        return a instanceof TypeLike || a instanceof Long;
    }

    public static <T> T checkValidArg(T a)
    {
        checkArgument(isValidArg(a));
        return a;
    }

    public static List<Type> objectsToTypes(List<Object> objects)
    {
        return objects.stream().map(Preconditions::checkNotNull).map(Type.class::cast).collect(toImmutableList());
    }

    public static Map<String, Type> objectsToTypes(Map<String, Object> objects)
    {
        return objects.entrySet().stream().map(e -> Pair.immutable(e.getKey(), (Type) checkNotNull(e.getValue()))).collect(toImmutableMap());
    }
}
