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

import java.util.Arrays;

public final class SimpleRow
        implements Row
{
    private final Id id;
    private final Object[] attributes;

    public SimpleRow(
            Id id,
            Object[] attributes)
    {
        this.id = id;
        this.attributes = attributes;
    }

    public static SimpleRow copyOf(Row row)
    {
        return row instanceof SimpleRow ? (SimpleRow) row : new SimpleRow(row.getId(), row.getAttributes());
    }

    @Override
    public String toString()
    {
        return "SimpleRow{" +
                "id=" + id +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

    @Override
    public Id getId()
    {
        return id;
    }

    @Override
    public Object[] getAttributes()
    {
        return attributes;
    }

    public static final JsonConverter<SimpleRow, Object[]> JSON_CONVERTER = new JsonConverter<>(
            SimpleRow.class,
            Object[].class,
            r -> new Object[] {r.id, r.attributes},
            a -> new SimpleRow((Id) a[0], (Object[]) a[1]));
}
