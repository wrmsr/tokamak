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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

public interface Pair<K, V>
        extends Map.Entry<K, V>
{
    K getFirst();

    V getSecond();

    default K first()
    {
        return getFirst();
    }

    default V second()
    {
        return getSecond();
    }

    default K getKey()
    {
        return getFirst();
    }

    default V getValue()
    {
        return getSecond();
    }

    @JsonSerialize(using = Pair.Serializer.class)
    @JsonDeserialize(using = Pair.ImmutableDeserializer.class)
    @javax.annotation.concurrent.Immutable
    final class Immutable<K, V>
            implements Pair<K, V>
    {
        private final K first;
        private final V second;

        public Immutable(K first, V second)
        {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Immutable<?, ?> pair = (Immutable<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(first, second);
        }

        @Override
        public String toString()
        {
            return "Pair.Immutable{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }

        @Override
        public K getFirst()
        {
            return first;
        }

        @Override
        public V getSecond()
        {
            return second;
        }

        @Override
        public V setValue(V second)
        {
            throw new UnsupportedOperationException();
        }
    }

    static <K, V> Immutable<K, V> immutable(K first, V second)
    {
        return new Immutable<>(first, second);
    }

    @JsonSerialize(using = Pair.Serializer.class)
    @JsonDeserialize(using = Pair.MutableDeserializer.class)
    final class Mutable<K, V>
            implements Pair<K, V>
    {
        private K first;
        private V second;

        public Mutable(K first, V second)
        {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Mutable<?, ?> pair = (Mutable<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(first, second);
        }

        @Override
        public String toString()
        {
            return "Pair.Mutable{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }

        @Override
        public K getFirst()
        {
            return first;
        }

        @Override
        public V getSecond()
        {
            return second;
        }

        public K setFirst(K first)
        {
            K old = this.first;
            this.first = first;
            return old;
        }

        public V setSecond(V second)
        {
            V old = this.second;
            this.second = second;
            return old;
        }

        @Override
        public V setValue(V second)
        {
            return setSecond(second);
        }
    }

    static <K, V> Mutable<K, V> mutable(K first, V second)
    {
        return new Mutable<>(first, second);
    }

    final class Serializer
            extends JsonSerializer<Pair<?, ?>>
    {
        @Override
        public void serialize(Pair<?, ?> value, JsonGenerator generator, SerializerProvider provider)
                throws IOException
        {
            generator.writeObject(ImmutableList.of(value.getFirst(), value.getSecond()));
        }
    }

    abstract class Deserializer<P extends Pair<?, ?>>
            extends JsonDeserializer<P>
            implements ContextualDeserializer
    {
        private JavaType firstType;
        private JavaType secondType;

        abstract Deserializer newDeserializer();

        abstract P newPair(Object first, Object second);

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property)
        {
            Pair.Deserializer deserializer = newDeserializer();
            if (property != null) {
                JavaType mapType = property.getType();
                deserializer.firstType = mapType.containedType(0);
                deserializer.secondType = mapType.containedType(1);
            }
            else {
                deserializer.firstType = ctx.getContextualType().containedType(0);
                deserializer.secondType = ctx.getContextualType().containedType(1);
            }
            return deserializer;
        }

        @Override
        public P deserialize(JsonParser parser, DeserializationContext ctx)
                throws IOException
        {
            checkState(parser.currentToken() == JsonToken.START_ARRAY);
            ObjectCodec codec = parser.getCodec();
            parser.nextToken();
            Object first = codec.readValue(codec.treeAsTokens(parser.readValueAsTree()), firstType);
            Object second = codec.readValue(codec.treeAsTokens(parser.readValueAsTree()), secondType);
            checkState(parser.nextToken() == JsonToken.END_ARRAY);
            return newPair(first, second);
        }
    }

    final class ImmutableDeserializer
            extends Deserializer<Immutable<?, ?>>
    {
        @Override
        Deserializer newDeserializer()
        {
            return new ImmutableDeserializer();
        }

        @Override
        Immutable<?, ?> newPair(Object first, Object second)
        {
            return new Immutable<>(first, second);
        }
    }

    final class MutableDeserializer
            extends Deserializer<Mutable<?, ?>>
    {
        @Override
        Deserializer newDeserializer()
        {
            return new MutableDeserializer();
        }

        @Override
        Mutable<?, ?> newPair(Object first, Object second)
        {
            return new Mutable<>(first, second);
        }
    }
}
