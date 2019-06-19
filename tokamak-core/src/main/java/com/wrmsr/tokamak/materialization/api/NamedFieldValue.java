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
package com.wrmsr.tokamak.materialization.api;

import java.util.Objects;

public final class NamedFieldValue<V>
        implements FieldValue<FieldName, V>
{
    private final FieldName field;
    private final V value;

    public NamedFieldValue(FieldName field, V value)
    {
        this.field = field;
        this.value = value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        NamedFieldValue<?> that = (NamedFieldValue<?>) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(field, value);
    }

    public FieldName getField()
    {
        return field;
    }

    public V getValue()
    {
        return value;
    }
}
