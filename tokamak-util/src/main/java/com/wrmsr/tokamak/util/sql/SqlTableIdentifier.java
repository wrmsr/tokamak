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
package com.wrmsr.tokamak.util.sql;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class SqlTableIdentifier
{
    private final @Nullable String catalog;
    private final @Nullable String schema;
    private final String name;

    public SqlTableIdentifier(@Nullable String catalog, @Nullable String schema, String name)
    {
        this.catalog = catalog;
        this.schema = schema;
        this.name = checkNotNull(name);
    }

    public static SqlTableIdentifier of(@Nullable String catalog, @Nullable String schema, String name)
    {
        return new SqlTableIdentifier(catalog, schema, name);
    }

    public static SqlTableIdentifier of(@Nullable String schema, String name)
    {
        return new SqlTableIdentifier(null, schema, name);
    }

    public static SqlTableIdentifier of(String name)
    {
        return new SqlTableIdentifier(null, null, name);
    }

    @Override
    public String toString()
    {
        return "TableIdentifier{" +
                "catalog='" + catalog + '\'' +
                ", schema='" + schema + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        SqlTableIdentifier that = (SqlTableIdentifier) o;
        return Objects.equals(catalog, that.catalog) &&
                Objects.equals(schema, that.schema) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(catalog, schema, name);
    }

    @Nullable
    public String getCatalog()
    {
        return catalog;
    }

    @Nullable
    public String getSchema()
    {
        return schema;
    }

    public String getName()
    {
        return name;
    }
}
