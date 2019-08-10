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
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

@JsonSerialize(using = OrderPreservingImmutableMap.Serializer.class)
@JsonDeserialize(using = OrderPreservingImmutableMap.Deserializer.class)
public final class OrderPreservingImmutableMap<K, V>
        implements Map<K, V>
{
    private final Map<K, V> map;

    public OrderPreservingImmutableMap(Map<K, V> map)
    {
        this.map = (map instanceof OrderPreservingImmutableMap) ? ((OrderPreservingImmutableMap<K, V>) map).map : ImmutableMap.copyOf(map);
    }

    public static final class Serializer
            extends JsonSerializer<OrderPreservingImmutableMap<?, ?>>
    {
        @Override
        public void serialize(OrderPreservingImmutableMap<?, ?> value, JsonGenerator generator, SerializerProvider provider)
                throws IOException
        {
            generator.writeObject(value.entrySet().stream().map(e -> ImmutableList.of(e.getKey(), e.getValue())).collect(toImmutableList()));
        }
    }

    public static final class Deserializer
            extends JsonDeserializer<OrderPreservingImmutableMap<?, ?>>
            implements ContextualDeserializer
    {
        private JavaType keyType;
        private JavaType valueType;

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property)
        {
            Deserializer deserializer = new Deserializer();
            if (property != null) {
                JavaType mapType = property.getType();
                deserializer.keyType = mapType.containedType(0);
                deserializer.valueType = mapType.containedType(1);
            }
            else {
                deserializer.keyType = ctx.getContextualType().containedType(0);
                deserializer.valueType = ctx.getContextualType().containedType(1);
            }
            return deserializer;
        }

        @Override
        public OrderPreservingImmutableMap<?, ?> deserialize(JsonParser parser, DeserializationContext ctx)
                throws IOException
        {
            checkState(parser.currentToken() == JsonToken.START_ARRAY);
            ImmutableMap.Builder builder = ImmutableMap.builder();
            while (true) {
                JsonToken tok = parser.nextToken();
                if (tok == JsonToken.END_ARRAY) {
                    break;
                }
                else if (tok == JsonToken.START_ARRAY) {
                    parser.nextToken();
                    Object key = ctx.readValue(parser, keyType);
                    Object value = ctx.readValue(parser, valueType);
                    checkState(parser.nextToken() == JsonToken.END_ARRAY);
                    builder.put(key, value);
                }
                else {
                    throw new IllegalStateException(tok.asString());
                }
            }
            return new OrderPreservingImmutableMap(builder.build());
        }
    }

    @Override
    public String toString()
    {
        return "OrderPreservingImmutableMap{" +
                "map=" + map +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        OrderPreservingImmutableMap<?, ?> that = (OrderPreservingImmutableMap<?, ?>) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(map);
    }

    @Override
    public int size() {return map.size();}

    @Override
    public boolean isEmpty() {return map.isEmpty();}

    @Override
    public boolean containsKey(Object key) {return map.containsKey(key);}

    @Override
    public boolean containsValue(Object value) {return map.containsValue(value);}

    @Override
    public V get(Object key) {return map.get(key);}

    @Override
    public V put(K key, V value) {return map.put(key, value);}

    @Override
    public V remove(Object key) {return map.remove(key);}

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {map.putAll(m);}

    @Override
    public void clear() {map.clear();}

    @Override
    public Set<K> keySet() {return map.keySet();}

    @Override
    public Collection<V> values() {return map.values();}

    @Override
    public Set<Entry<K, V>> entrySet() {return map.entrySet();}

    @Override
    public V getOrDefault(Object key, V defaultValue) {return map.getOrDefault(key, defaultValue);}

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {map.forEach(action);}

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {map.replaceAll(function);}

    @Override
    public V putIfAbsent(K key, V value) {return map.putIfAbsent(key, value);}

    @Override
    public boolean remove(Object key, Object value) {return map.remove(key, value);}

    @Override
    public boolean replace(K key, V oldValue, V newValue) {return map.replace(key, oldValue, newValue);}

    @Override
    public V replace(K key, V value) {return map.replace(key, value);}

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {return map.computeIfAbsent(key, mappingFunction);}

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {return map.computeIfPresent(key, remappingFunction);}

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {return map.compute(key, remappingFunction);}

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {return map.merge(key, value, remappingFunction);}
}
