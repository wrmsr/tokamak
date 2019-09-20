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
package com.wrmsr.tokamak.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.wrmsr.tokamak.api.Util.checkNotNull;

public final class Key
        implements Iterable<Map.Entry<String, Object>>
{
    private final Map<String, Object> valuesByField;

    public Key(Map<String, Object> valuesByField)
    {
        this.valuesByField = checkNotNull(valuesByField);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Key entries = (Key) o;
        return Objects.equals(valuesByField, entries.valuesByField);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(valuesByField);
    }

    @Override
    public String toString()
    {
        return "Key{" + valuesByField + '}';
    }

    public Map<String, Object> getValuesByField()
    {
        return Collections.unmodifiableMap(valuesByField);
    }

    public Object get(String field)
    {
        return valuesByField.get(field);
    }

    public Set<String> getFields()
    {
        return valuesByField.keySet();
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator()
    {
        return valuesByField.entrySet().iterator();
    }

    public Stream<Map.Entry<String, Object>> stream()
    {
        return valuesByField.entrySet().stream();
    }

    @SuppressWarnings({"unchecked"})
    public static final JsonConverter<Key, Map> JSON_CONVERTER = new JsonConverter<>(
            Key.class,
            Map.class,
            id -> id.valuesByField,
            map -> of((Map<String, Object>) map));

    public static Key of(Map<String, Object> map)
    {
        return new Key(map);
    }

    public static Key of(String field0, Object value0)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(field0, value0);
        return new Key(map);
    }

    public static Key of(String field0, Object value0, String field1, Object value1)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(field0, value0);
        map.put(field1, value1);
        return new Key(map);
    }

    public static Key of(String field0, Object value0, String field1, Object value1, String field2, Object value2)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(field0, value0);
        map.put(field1, value1);
        map.put(field2, value2);
        return new Key(map);
    }

    private static final Key ALL = new Key(new HashMap<>());

    public static Key all()
    {
        return ALL;
    }
}
