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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.List;

public class JsonTest
        extends TestCase
{
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "name")
    public static class Link
    {
        public final @JsonProperty("name") String name;
        public final @JsonProperty("links") List<Link> links;

        @JsonCreator
        public Link(@JsonProperty("name") String name, @JsonProperty("links") List<Link> links)
        {
            this.name = name;
            this.links = links;
        }

        @Override
        public String toString()
        {
            return "Link@" + Integer.toString(System.identityHashCode(this), 16) + "{" +
                    "name='" + name + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    public void testReferenceJson()
            throws Throwable
    {
        Link a = new Link("a", ImmutableList.of());
        Link b = new Link("b", ImmutableList.of(a));
        Link c = new Link("c", ImmutableList.of(a));
        Link d = new Link("d", ImmutableList.of(b, c));

        System.out.println(d);

        String src = Json.writeValue(d);
        System.out.println(src);

        Link jl = Json.readValue(src, Link.class);
        System.out.println(jl);
    }

    /*
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = LinkableImpl.class, name = "impl"),
    })
    public static abstract class Linkable
    {
        @JsonProperty("name")
        public final String name;

        public Linkable(String name)
        {
            this.name = name;
        }
    }

    public static class LinkableImpl
            extends Linkable
    {
        public static final class E
        {
            @JsonIdentityInfo(
                    generator = ObjectIdGenerators.PropertyGenerator.class,
                    property = "name")
            @JsonProperty("link")
            public final Linkable link;

            @JsonCreator
            public E(@JsonProperty("link") Linkable link)
            {
                this.link = link;
            }

            public static E of(Linkable link)
            {
                return new E(link);
            }
        }

        @JsonProperty("links")
        public final List<E> links;

        @JsonCreator
        public LinkableImpl(
                @JsonProperty("name") String name,
                @JsonProperty("links") List<E> links)
        {
            super(name);
            this.links = links;
        }

        @Override
        public String toString()
        {
            return "LinkableImpl@" + System.identityHashCode(this) + "{" +
                    "name='" + name + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    public void testIfaceReferenceJson()
            throws Throwable
    {
        Linkable a = new LinkableImpl("a", ImmutableList.of());
        Linkable b = new LinkableImpl("b", ImmutableList.of(LinkableImpl.E.of(a)));
        Linkable c = new LinkableImpl("c", ImmutableList.of(LinkableImpl.E.of(a)));
        Linkable d = new LinkableImpl("d", ImmutableList.of(LinkableImpl.E.of(b), LinkableImpl.E.of(c)));

        System.out.println(d);

        String src = Json.writeValue(d);
        System.out.println(src);

        Linkable jl = Json.readValue(src, Linkable.class);
        System.out.println(jl);
    }
    */

    // public static final class LinkableSerializer
    //         extends JsonSerializer<Linkable>
    //         implements ContextualSerializer
    // {
    //     public LinkableSerializer()
    //     {
    //     }
    //
    //     @Override
    //     public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
    //             throws JsonMappingException
    //     {
    //         return this;
    //     }
    //
    //     @Override
    //     public void serialize(Linkable value, JsonGenerator generator, SerializerProvider provider)
    //             throws IOException
    //     {
    //         throw new IllegalStateException();
    //     }
    //
    //     private class Factory
    //             extends BeanSerializerFactory
    //     {
    //         private final SerializationConfig serializationConfig;
    //
    //         public Factory(SerializerFactoryConfig config, SerializationConfig serializationConfig)
    //         {
    //             super(config);
    //             this.serializationConfig = serializationConfig;
    //         }
    //
    //         class Builder extends BeanSerializerBuilder
    //         {
    //             public Builder(BeanDescription beanDesc)
    //             {
    //                 super(beanDesc);
    //             }
    //
    //             @Override
    //             public void setConfig(SerializationConfig config)
    //             {
    //                 super.setConfig(config);
    //             }
    //         }
    //
    //         @Override
    //         public BeanSerializer constructBeanSerializer(SerializerProvider prov, BeanDescription beanDesc)
    //                 throws JsonMappingException
    //         {
    //             final SerializationConfig config = prov.getConfig();
    //             Builder builder = new Builder(beanDesc);
    //             builder.setConfig(config);
    //
    //             // First: any detectable (auto-detect, annotations) properties to serialize?
    //             List<BeanPropertyWriter> props = findBeanProperties(prov, beanDesc, builder);
    //             if (props == null) {
    //                 props = new ArrayList<>();
    //             }
    //             else {
    //                 props = removeOverlappingTypeIds(prov, beanDesc, builder, props);
    //             }
    //
    //             // [databind#638]: Allow injection of "virtual" properties:
    //             prov.getAnnotationIntrospector().findAndAddVirtualProperties(config, beanDesc.getClassInfo(), props);
    //
    //             builder.setProperties(props);
    //
    //             return (BeanSerializer) builder.build();
    //         }
    //     }
    //
    //     @Override
    //     public void serializeWithType(Linkable value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
    //             throws IOException
    //     {
    //         // if (_objectIdWriter != null) {
    //         //     gen.setCurrentValue(value); // [databind#631]
    //         //     _serializeWithObjectId(bean, gen, provider, typeSer);
    //         //     return;
    //         // }
    //
    //         gen.setCurrentValue(value);  // FIXME: everywhere
    //         WritableTypeId typeIdDef = typeSer.typeId(value, JsonToken.START_OBJECT);
    //         typeSer.writeTypePrefix(gen, typeIdDef);
    //
    //         BeanDescription beanDesc = serializers.getConfig().introspect(SimpleType.construct(value.getClass()));
    //         BeanSerializer ser = new Factory(
    //                 new SerializerFactoryConfig(),
    //                 ((ObjectMapper) gen.getCodec()).getSerializationConfig()
    //         ).constructBeanSerializer(serializers, beanDesc);
    //
    //         gen.writeFieldName("what");
    //         ser.serialize(value, gen, serializers);
    //
    //         typeSer.writeTypeSuffix(gen, typeIdDef);
    //     }
    // }
    //
    // public static final class LinkableDeserializer
    //         extends JsonDeserializer<Linkable>
    //         implements ContextualDeserializer
    // {
    //     public LinkableDeserializer()
    //     {
    //     }
    //
    //     @Override
    //     public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
    //             throws JsonMappingException
    //     {
    //         return this;
    //     }
    //
    //     @Override
    //     public Linkable deserialize(JsonParser p, DeserializationContext ctxt)
    //             throws IOException, JsonProcessingException
    //     {
    //         throw new IllegalStateException();
    //     }
    // }
    //
    // @JsonTypeInfo(
    //         use = JsonTypeInfo.Id.NAME,
    //         include = JsonTypeInfo.As.WRAPPER_OBJECT)
    // @JsonSubTypes({
    //         @JsonSubTypes.Type(value = LinkableImpl.class, name = "impl"),
    // })
    // @JsonSerialize(using = LinkableSerializer.class)
    // @JsonDeserialize(using = LinkableDeserializer.class)
    // public interface Linkable
    // {
    //     String getName();
    // }
    //
    // public static class LinkableImpl
    //         implements Linkable
    // {
    //     protected final String name;
    //
    //     @JsonProperty("links")
    //     public final List<Linkable> links;
    //
    //     @JsonProperty("name")
    //     @Override
    //     public String getName()
    //     {
    //         return name;
    //     }
    //
    //     @JsonCreator
    //     public LinkableImpl(
    //             @JsonProperty("name") String name,
    //             @JsonProperty("links") List<Linkable> links)
    //     {
    //         this.name = name;
    //         this.links = links;
    //     }
    //
    //     @Override
    //     public String toString()
    //     {
    //         return "LinkableImpl@" + Integer.toString(System.identityHashCode(this), 16) + "{" +
    //                 "name='" + getName() + '\'' +
    //                 ", links=" + links +
    //                 '}';
    //     }
    // }
    //
    // public void testIfaceReferenceJson()
    //         throws Throwable
    // {
    //     Linkable a = new LinkableImpl("a", ImmutableList.of());
    //     Linkable b = new LinkableImpl("b", ImmutableList.of(a));
    //     Linkable c = new LinkableImpl("c", ImmutableList.of(a));
    //     Linkable d = new LinkableImpl("d", ImmutableList.of(b, c));
    //
    //     System.out.println(d);
    //
    //     String src = Json.writeValue(d);
    //     System.out.println(src);
    //
    //     Linkable jl = Json.readValue(src, Linkable.class);
    //     System.out.println(jl);
    // }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = LinkableImpl.class, name = "impl"),
    })
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    public interface Linkable
    {
        String getName();
    }

    public static class LinkableImpl
            implements Linkable
    {
        protected final String name;

        @JsonProperty("links")
        public final List<Linkable> links;

        @JsonProperty("name")
        @Override
        public String getName()
        {
            return name;
        }

        @JsonCreator
        public LinkableImpl(
                @JsonProperty("name") String name,
                @JsonProperty("links") List<Linkable> links)
        {
            this.name = name;
            this.links = links;
        }

        @Override
        public String toString()
        {
            return "LinkableImpl@" + Integer.toString(System.identityHashCode(this), 16) + "{" +
                    "name='" + getName() + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    public void testIfaceReferenceJson()
            throws Throwable
    {
        Linkable a = new LinkableImpl("a", ImmutableList.of());
        Linkable b = new LinkableImpl("b", ImmutableList.of(a));
        Linkable c = new LinkableImpl("c", ImmutableList.of(a));
        Linkable d = new LinkableImpl("d", ImmutableList.of(b, c));

        System.out.println(d);

        String src = Json.writeValue(d);
        System.out.println(src);

        Linkable jl = Json.readValue(src, Linkable.class);
        System.out.println(jl);
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    public interface IdThing
    {
        int getValue();
    }

    public static final class IdThingImpl
            implements IdThing
    {
        private final int value;

        @JsonCreator
        public IdThingImpl(@JsonProperty("value") int value)
        {
            this.value = value;
        }

        @JsonProperty("value")
        @Override
        public int getValue()
        {
            return value;
        }
    }

    public void testIdThing()
            throws Throwable
    {
        ObjectMapper om = Json.newObjectMapper();
        Collection<NamedType> col = om.getSubtypeResolver().collectAndResolveSubtypesByTypeId(
                om.getSerializationConfig(),
                om.getSerializationConfig().introspect(om.getTypeFactory().constructType(IdThingImpl.class)).getClassInfo());
        om.registerSubtypes(new NamedType(IdThingImpl.class, "impl"));
        col = om.getSubtypeResolver().collectAndResolveSubtypesByTypeId(
                om.getSerializationConfig(),
                om.getSerializationConfig().introspect(om.getTypeFactory().constructType(IdThingImpl.class)).getClassInfo());
        String src = om.writeValueAsString(new IdThingImpl(420));
        System.out.println(src);
    }
}
