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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.util.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class FieldKey
        implements Key, StreamableIterable<Map.Entry<String, Object>>
{
    private final Map<String, Object> valuesByField;

    public FieldKey(
            Map<String, Object> valuesByField)
    {
        this.valuesByField = ImmutableMap.copyOf(valuesByField);
        checkArgument(!valuesByField.isEmpty());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        FieldKey entries = (FieldKey) o;
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
        return "FieldKey{" +
                "valuesByField=" + valuesByField +
                '}';
    }

    public Map<String, Object> getValuesByField()
    {
        return valuesByField;
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator()
    {
        return valuesByField.entrySet().iterator();
    }

    @JsonCreator
    public static FieldKey parsePrefixed(Map<String, Object> valuesByField)
    {
        return new FieldKey(valuesByField);
    }

    @JsonValue
    public Map<String, Object> toPrefixedString()
    {
        return valuesByField;
    }

    @Override
    public int compareTo(Key o)
    {
        if (o instanceof FieldKey) {
            FieldKey ofk = (FieldKey) o;

            List<String> lk = valuesByField.keySet().stream().sorted().collect(Collectors.toList());
            List<String> rk = ofk.valuesByField.keySet().stream().sorted().collect(Collectors.toList());
            int cmp = Comparators.lexicographical(String::compareTo).compare(lk, rk);
            if (cmp != 0) {
                return cmp;
            }

            for (String key : lk) {
                Object l = checkNotNull(valuesByField.get(key));
                Object r = checkNotNull(ofk.valuesByField.get(key));
                cmp = ((Comparable) l).compareTo(r);
                if (cmp != 0) {
                    return cmp;
                }
            }

            return 0;
        }
        else if (o instanceof AllKey) {
            return -1;
        }
        else if (o instanceof IdKey) {
            return 1;
        }
        else {
            throw new IllegalArgumentException(o.toString());
        }
    }
}
