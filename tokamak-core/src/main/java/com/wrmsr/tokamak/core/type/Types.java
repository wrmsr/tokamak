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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeAnnotation;
import com.wrmsr.tokamak.core.type.hier.TypeLike;
import com.wrmsr.tokamak.core.type.hier.annotation.EphemeralType;
import com.wrmsr.tokamak.core.type.hier.annotation.InternalType;
import com.wrmsr.tokamak.core.type.hier.annotation.NotNullType;
import com.wrmsr.tokamak.core.type.hier.annotation.SizedType;
import com.wrmsr.tokamak.core.type.hier.collection.CollectionType;
import com.wrmsr.tokamak.core.type.hier.collection.item.EnumSetType;
import com.wrmsr.tokamak.core.type.hier.collection.item.ItemType;
import com.wrmsr.tokamak.core.type.hier.collection.item.ListType;
import com.wrmsr.tokamak.core.type.hier.collection.item.SetType;
import com.wrmsr.tokamak.core.type.hier.collection.keyvalue.BiMapType;
import com.wrmsr.tokamak.core.type.hier.collection.keyvalue.KeyValueType;
import com.wrmsr.tokamak.core.type.hier.collection.keyvalue.MapType;
import com.wrmsr.tokamak.core.type.hier.primitive.BooleanType;
import com.wrmsr.tokamak.core.type.hier.primitive.DoubleType;
import com.wrmsr.tokamak.core.type.hier.primitive.LongType;
import com.wrmsr.tokamak.core.type.hier.primitive.PrimitiveType;
import com.wrmsr.tokamak.core.type.hier.simple.BigDecimalType;
import com.wrmsr.tokamak.core.type.hier.simple.BigIntegerType;
import com.wrmsr.tokamak.core.type.hier.simple.BytesType;
import com.wrmsr.tokamak.core.type.hier.simple.ObjectType;
import com.wrmsr.tokamak.core.type.hier.simple.SimpleType;
import com.wrmsr.tokamak.core.type.hier.simple.StringType;
import com.wrmsr.tokamak.core.type.hier.simple.VoidType;
import com.wrmsr.tokamak.core.type.hier.simple.time.DurationType;
import com.wrmsr.tokamak.core.type.hier.simple.time.LocalDateTimeType;
import com.wrmsr.tokamak.core.type.hier.simple.time.LocalDateType;
import com.wrmsr.tokamak.core.type.hier.simple.time.LocalTimeType;
import com.wrmsr.tokamak.core.type.hier.simple.time.TimeType;
import com.wrmsr.tokamak.core.type.hier.simple.time.ZonedDateTimeType;
import com.wrmsr.tokamak.core.type.hier.special.AnnotatedType;
import com.wrmsr.tokamak.core.type.hier.special.EnumType;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;
import com.wrmsr.tokamak.core.type.hier.special.SpecialType;
import com.wrmsr.tokamak.core.type.hier.special.TupleType;
import com.wrmsr.tokamak.core.type.hier.special.UnionType;
import com.wrmsr.tokamak.core.type.hier.special.struct.StructType;
import com.wrmsr.tokamak.core.type.hier.special.struct.StructuralType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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

    //region Setup

    private static final ImmutableSet.Builder<Class<? extends TypeLike>> INTERNAL_TYPE_INTERFACES_BUILDER = ImmutableSet.builder();
    private static final ImmutableList.Builder<TypeRegistration> TYPE_REGISTRATION_BUILDER = ImmutableList.builder();

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(Type.class)
                .add(TypeAnnotation.class)
                .add(TypeLike.class);
    }

    //endregion

    //region Annotation

    static {
        TYPE_REGISTRATION_BUILDER
                .add(EphemeralType.REGISTRATION)
                .add(InternalType.REGISTRATION)
                .add(NotNullType.REGISTRATION)
                .add(SizedType.REGISTRATION);
    }

    public static Type Ephemeral(Type item)
    {
        return TypeAnnotations.map(item, anns -> anns.appended(EphemeralType.INSTANCE));
    }

    public static Type Internal(Type item)
    {
        return TypeAnnotations.map(item, anns -> anns.appended(InternalType.INSTANCE));
    }

    public static Type NotNull(Type item)
    {
        return TypeAnnotations.map(item, anns -> anns.appended(NotNullType.INSTANCE));
    }

    public static Type Sized(long size, Type item)
    {
        return TypeAnnotations.map(item, anns -> anns.appended(new SizedType(size)));
    }

    //endregion

    //region Collection

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(CollectionType.class);
    }

    //region Item

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(ItemType.class);

        TYPE_REGISTRATION_BUILDER
                .add(EnumSetType.REGISTRATION)
                .add(ListType.REGISTRATION)
                .add(SetType.REGISTRATION);
    }

    public static EnumSetType EnumSet(EnumType item)
    {
        return new EnumSetType(item);
    }

    public static ListType List(Type item)
    {
        return new ListType(item);
    }

    public static SetType Set(Type item)
    {
        return new SetType(item);
    }

    //endregion

    //region KeyValue

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(KeyValueType.class);

        TYPE_REGISTRATION_BUILDER
                .add(BiMapType.REGISTRATION)
                .add(MapType.REGISTRATION);
    }

    public static BiMapType BiMap(Type key, Type value)
    {
        return new BiMapType(key, value);
    }

    public static MapType Map(Type key, Type value)
    {
        return new MapType(key, value);
    }

    //endregion

    //endregion

    //region Primitive

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(PrimitiveType.class);

        TYPE_REGISTRATION_BUILDER
                .add(BooleanType.REGISTRATION)
                .add(DoubleType.REGISTRATION)
                .add(LongType.REGISTRATION);
    }

    public static BooleanType Boolean()
    {
        return BooleanType.INSTANCE;
    }

    public static DoubleType Double()
    {
        return DoubleType.INSTANCE;
    }

    public static LongType Long()
    {
        return LongType.INSTANCE;
    }

    //endregion

    //region Simple

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(SimpleType.class);

        TYPE_REGISTRATION_BUILDER
                .add(BigDecimalType.REGISTRATION)
                .add(BigIntegerType.REGISTRATION)
                .add(BytesType.REGISTRATION)
                .add(ObjectType.REGISTRATION)
                .add(StringType.REGISTRATION)
                .add(VoidType.REGISTRATION);
    }

    public static BigDecimalType BigDecimal()
    {
        return BigDecimalType.INSTANCE;
    }

    public static BigIntegerType BigInteger()
    {
        return BigIntegerType.INSTANCE;
    }

    public static BytesType Bytes()
    {
        return BytesType.INSTANCE;
    }

    public static ObjectType Object()
    {
        return ObjectType.INSTANCE;
    }

    public static StringType String()
    {
        return StringType.INSTANCE;
    }

    public static VoidType Void()
    {
        return VoidType.INSTANCE;
    }

    //region Time

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(TimeType.class);

        TYPE_REGISTRATION_BUILDER
                .add(DurationType.REGISTRATION)
                .add(LocalDateTimeType.REGISTRATION)
                .add(LocalDateType.REGISTRATION)
                .add(LocalTimeType.REGISTRATION)
                .add(ZonedDateTimeType.REGISTRATION);
    }

    public static DurationType Duration()
    {
        return DurationType.INSTANCE;
    }

    public static LocalDateTimeType LocalDateTime()
    {
        return LocalDateTimeType.INSTANCE;
    }

    public static LocalDateType LocalDate()
    {
        return LocalDateType.INSTANCE;
    }

    public static LocalTimeType LocalTime()
    {
        return LocalTimeType.INSTANCE;
    }

    public static ZonedDateTimeType ZonedDateTime()
    {
        return ZonedDateTimeType.INSTANCE;
    }

    //endregion

    //endregion

    //region Struct

    static {
        TYPE_REGISTRATION_BUILDER
                .add(StructType.REGISTRATION)
                .add(StructuralType.REGISTRATION);
    }

    public static StructType Struct(Map<String, Type> members)
    {
        return new StructType(members);
    }

    public static StructuralType Structural(Map<String, Type> members)
    {
        return new StructuralType(members);
    }

    //endregion

    //region Special

    static {
        INTERNAL_TYPE_INTERFACES_BUILDER
                .add(SpecialType.class);

        TYPE_REGISTRATION_BUILDER
                .add(AnnotatedType.REGISTRATION)
                .add(EnumType.REGISTRATION)
                .add(FunctionType.REGISTRATION)
                .add(TupleType.REGISTRATION)
                .add(UnionType.REGISTRATION);
    }

    public static AnnotatedType Annotated(TypeAnnotation annotation0, Type item)
    {
        return Annotated(ImmutableList.of(annotation0), item);
    }

    public static AnnotatedType Annotated(TypeAnnotation annotation0, TypeAnnotation annotation1, Type item)
    {
        return Annotated(ImmutableList.of(annotation0, annotation1), item);
    }

    public static AnnotatedType Annotated(TypeAnnotation annotation0, TypeAnnotation annotation1, TypeAnnotation annotation2, Type item)
    {
        return Annotated(ImmutableList.of(annotation0, annotation1, annotation2), item);
    }

    public static AnnotatedType Annotated(Iterable<TypeAnnotation> annotations, Type item)
    {
        return new AnnotatedType(annotations, item);
    }

    public static EnumType Enum(Map<String, Long> values)
    {
        return new EnumType(values);
    }

    public static FunctionType Function(Type value)
    {
        return new FunctionType(value, ImmutableList.of());
    }

    public static FunctionType Function(Type value, Type... params)
    {
        return new FunctionType(value, ImmutableList.copyOf(params));
    }

    public static FunctionType Function(Type value, Iterable<Type> params)
    {
        return new FunctionType(value, params);
    }

    public static TupleType Tuple(Type... items)
    {
        return new TupleType(ImmutableList.copyOf(items));
    }

    public static UnionType Union(Type... items)
    {
        return new UnionType(ImmutableList.copyOf(items));
    }

    //endregion

    public static final Set<Class<? extends TypeLike>> INTERNAL_TYPE_INTERFACES = INTERNAL_TYPE_INTERFACES_BUILDER.build();

    public static final List<TypeRegistration> BUILTIN_REGISTRATIONS = TYPE_REGISTRATION_BUILDER.build();

    public static final TypeRegistry BUILTIN_REGISTRY = ((Supplier<TypeRegistry>) () -> {
        TypeRegistry registry = new TypeRegistry();
        BUILTIN_REGISTRATIONS.forEach(registry::register);
        return registry;
    }).get();
}
