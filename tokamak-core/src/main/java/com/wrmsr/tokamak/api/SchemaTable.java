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

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class SchemaTable
{
    private final String schema;
    private final String table;

    public SchemaTable(String schema, String table)
    {
        this.schema = checkNotNull(schema);
        this.table = checkNotNull(table);
    }

    public static SchemaTable of(String schema, String table)
    {
        return new SchemaTable(schema, table);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        SchemaTable that = (SchemaTable) o;
        return Objects.equals(schema, that.schema) &&
                Objects.equals(table, that.table);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(schema, table);
    }

    @Override
    public String toString()
    {
        return "SchemaTable{" +
                "schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                '}';
    }

    public String getSchema()
    {
        return schema;
    }

    public String getTable()
    {
        return table;
    }

    @JsonCreator
    public static SchemaTable fromDotString(String dotString)
    {
        String[] parts = dotString.split("\\.");
        checkArgument(parts.length == 2);
        return new SchemaTable(parts[0], parts[1]);
    }

    @JsonValue
    public String toDotString()
    {
        return String.format("%s.%s", schema, table);
    }
}
