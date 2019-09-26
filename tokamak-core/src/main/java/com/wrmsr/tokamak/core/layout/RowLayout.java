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
package com.wrmsr.tokamak.core.layout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.StructType;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.collect.ObjectArrayBackedMap;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static java.util.function.Function.identity;

@Immutable
public final class RowLayout
{
    private final Map<String, Type> fields;

    private final List<String> fieldNames;
    private final Map<String, Integer> positionsByField;

    private final StructType structType;
    private final ObjectArrayBackedMap.Shape<String> shape;

    @JsonCreator
    public RowLayout(
            @JsonProperty("fields") Map<String, Type> fields)
    {
        this.fields = ImmutableMap.copyOf(checkOrdered(fields));

        fieldNames = ImmutableList.copyOf(this.fields.keySet());
        positionsByField = IntStream.range(0, fields.size()).boxed().collect(toImmutableMap(fieldNames::get, identity()));

        structType = new StructType(ImmutableMap.copyOf(this.fields));
        shape = ObjectArrayBackedMap.Shape.of(fieldNames);
    }

    @Override
    public String toString()
    {
        return "RowLayout{" +
                "fields=" + fields +
                '}';
    }

    @JsonProperty("fields")
    public Map<String, Type> getFields()
    {
        return fields;
    }

    public List<String> getFieldNames()
    {
        return fieldNames;
    }

    public Map<String, Integer> getPositionsByField()
    {
        return positionsByField;
    }

    public StructType getStructType()
    {
        return structType;
    }

    public ObjectArrayBackedMap.Shape<String> getShape()
    {
        return shape;
    }

    public Map<String, Object> arrayToMap(Object[] array)
    {
        checkNotNull(array);
        checkArgument(array.length == fields.size());
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builderWithExpectedSize(fields.size());
        for (int i = 0; i < array.length; ++i) {
            builder.put(fieldNames.get(i), array[i]);
        }
        return builder.build();
    }

    public Object[] mapToArray(Map<String, Object> map, boolean strict)
    {
        checkNotNull(map);
        Object[] arr = new Object[fields.size()];
        map.forEach((k, v) -> {
            Integer pos = positionsByField.get(k);
            if (pos != null) {
                arr[pos] = v;
            }
            else if (strict) {
                throw new IllegalArgumentException("Unexpected key: " + k);
            }
        });
        return arr;
    }

    public Object[] mapToArray(Map<String, Object> map)
    {
        return mapToArray(map, true);
    }
}