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
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeLike;
import com.wrmsr.tokamak.core.type.hier.annotation.InternalType;
import com.wrmsr.tokamak.core.type.hier.annotation.NotNullType;
import com.wrmsr.tokamak.core.type.hier.annotation.SizedType;
import com.wrmsr.tokamak.core.type.hier.collection.item.EnumSetType;
import com.wrmsr.tokamak.core.type.hier.collection.item.ListType;
import com.wrmsr.tokamak.core.type.hier.collection.item.SetType;
import com.wrmsr.tokamak.core.type.hier.collection.keyvalue.BiMapType;
import com.wrmsr.tokamak.core.type.hier.collection.keyvalue.MapType;
import com.wrmsr.tokamak.core.type.hier.primitive.BooleanType;
import com.wrmsr.tokamak.core.type.hier.primitive.DoubleType;
import com.wrmsr.tokamak.core.type.hier.primitive.LongType;
import com.wrmsr.tokamak.core.type.hier.simple.BigDecimalType;
import com.wrmsr.tokamak.core.type.hier.simple.BigIntegerType;
import com.wrmsr.tokamak.core.type.hier.simple.BytesType;
import com.wrmsr.tokamak.core.type.hier.simple.ObjectType;
import com.wrmsr.tokamak.core.type.hier.simple.StringType;
import com.wrmsr.tokamak.core.type.hier.simple.VoidType;
import com.wrmsr.tokamak.core.type.hier.simple.time.DurationType;
import com.wrmsr.tokamak.core.type.hier.simple.time.LocalDateTimeType;
import com.wrmsr.tokamak.core.type.hier.simple.time.LocalDateType;
import com.wrmsr.tokamak.core.type.hier.simple.time.LocalTimeType;
import com.wrmsr.tokamak.core.type.hier.simple.time.ZonedDateTimeType;
import com.wrmsr.tokamak.core.type.hier.special.AnnotatedType;
import com.wrmsr.tokamak.core.type.hier.special.EnumType;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;
import com.wrmsr.tokamak.core.type.hier.special.TupleType;
import com.wrmsr.tokamak.core.type.hier.special.UnionType;
import com.wrmsr.tokamak.core.type.hier.special.struct.StructType;
import com.wrmsr.tokamak.core.type.hier.special.struct.StructuralType;
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

    public static final List<TypeRegistration> BUILTIN_REGISTRATIONS = ImmutableList.<TypeRegistration>builder()

            .add(InternalType.REGISTRATION)
            .add(NotNullType.REGISTRATION)
            .add(SizedType.REGISTRATION)

            .add(EnumSetType.REGISTRATION)
            .add(ListType.REGISTRATION)
            .add(SetType.REGISTRATION)

            .add(BiMapType.REGISTRATION)
            .add(MapType.REGISTRATION)

            .add(BooleanType.REGISTRATION)
            .add(DoubleType.REGISTRATION)
            .add(LongType.REGISTRATION)

            .add(DurationType.REGISTRATION)
            .add(LocalDateTimeType.REGISTRATION)
            .add(LocalDateType.REGISTRATION)
            .add(LocalTimeType.REGISTRATION)
            .add(ZonedDateTimeType.REGISTRATION)

            .add(BigDecimalType.REGISTRATION)
            .add(BigIntegerType.REGISTRATION)
            .add(BytesType.REGISTRATION)
            .add(ObjectType.REGISTRATION)
            .add(StringType.REGISTRATION)
            .add(VoidType.REGISTRATION)

            .add(StructType.REGISTRATION)
            .add(StructuralType.REGISTRATION)

            .add(AnnotatedType.REGISTRATION)
            .add(EnumType.REGISTRATION)
            .add(FunctionType.REGISTRATION)
            .add(TupleType.REGISTRATION)
            .add(UnionType.REGISTRATION)

            .build();

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
