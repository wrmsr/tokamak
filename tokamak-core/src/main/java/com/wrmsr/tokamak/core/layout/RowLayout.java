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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.type.hier.special.StructType;
import com.wrmsr.tokamak.util.collect.ObjectArrayBackedMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class RowLayout
{
    private final FieldCollection fields;

    private final StructType structType;
    private final ObjectArrayBackedMap.Shape<String> shape;

    @JsonCreator
    public RowLayout(
            @JsonProperty("fields") FieldCollection fields)
    {
        this.fields = checkNotNull(fields);

        structType = new StructType(ImmutableMap.copyOf(fields.getTypesByName()));
        shape = ObjectArrayBackedMap.Shape.of(fields.getNameList());
    }

    @Override
    public String toString()
    {
        return "RowLayout{" +
                "fields=" + fields +
                '}';
    }

    @JsonProperty("fields")
    public FieldCollection getFields()
    {
        return fields;
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
            builder.put(fields.getNameList().get(i), array[i]);
        }
        return builder.build();
    }

    public enum MapToArrayStrictness
    {
        NONE(false, false),
        REJECT_UNEXPECTED(true, false),
        REJECT_MISSING(false, true),
        FULL(true, true);

        private final boolean rejectUnexpected;
        private final boolean rejectMissing;

        MapToArrayStrictness(
                boolean rejectUnexpected,
                boolean rejectMissing)
        {
            this.rejectUnexpected = rejectUnexpected;
            this.rejectMissing = rejectMissing;
        }
    }

    public Object[] mapToArray(Map<String, Object> map, MapToArrayStrictness strictness)
    {
        checkNotNull(map);
        checkNotNull(strictness);
        Object[] arr = new Object[fields.size()];
        int seen = 0;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            Integer pos = fields.getPositionsByName().get(e.getKey());
            if (pos != null) {
                arr[pos] = e.getValue();
            }
            else if (strictness.rejectUnexpected) {
                throw new IllegalArgumentException("Unexpected key: " + e.getKey());
            }
            ++seen;
        }
        if (strictness.rejectMissing && seen != fields.size()) {
            Set<String> missing = Sets.difference(fields.getNames(), map.keySet());
            throw new IllegalArgumentException("Missing keys: " + missing);
        }
        return arr;
    }

    public Object[] mapToArray(Map<String, Object> map)
    {
        return mapToArray(map, MapToArrayStrictness.FULL);
    }
}
