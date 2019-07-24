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
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

@Immutable
public final class FieldKey<V>
        implements Key
{
    private final FieldName field;
    private final V value;

    @JsonCreator
    public FieldKey(
            @JsonProperty("field") FieldName field,
            @JsonProperty("value") V value)
    {
        this.field = field;
        this.value = value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        FieldKey<?> that = (FieldKey<?>) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(field, value);
    }

    @JsonProperty("field")
    public FieldName getField()
    {
        return field;
    }

    @JsonProperty("value")
    public V getValue()
    {
        return value;
    }
}
