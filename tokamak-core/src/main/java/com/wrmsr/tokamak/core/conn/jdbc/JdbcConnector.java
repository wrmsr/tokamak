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
package com.wrmsr.tokamak.core.conn.jdbc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Connection;
import com.wrmsr.tokamak.core.catalog.Connector;
import com.wrmsr.tokamak.core.catalog.Scanner;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.layout.TableLayout;
import com.wrmsr.tokamak.util.sql.SqlConnection;
import com.wrmsr.tokamak.util.sql.SqlEngine;
import com.wrmsr.tokamak.util.sql.SqlTableIdentifier;
import com.wrmsr.tokamak.util.sql.metadata.MetaDataReflection;
import com.wrmsr.tokamak.util.sql.metadata.TableDescription;

import javax.annotation.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class JdbcConnector
        implements Connector
{
    private final String name;
    private final SqlEngine sqlEngine;
    private final @Nullable String catalog;

    @JsonCreator
    public JdbcConnector(
            @JsonProperty("name") String name,
            @JsonProperty("sqlEngine") SqlEngine sqlEngine,
            @JsonProperty("catalog") String catalog)
    {
        this.name = checkNotEmpty(name);
        this.sqlEngine = checkNotNull(sqlEngine);
        this.catalog = catalog;
    }

    public JdbcConnector(String name, SqlEngine sqlEngine)
    {
        this(name, sqlEngine, null);
    }

    @Override
    public String toString()
    {
        return "JdbcConnector{" +
                "name='" + name + '\'' +
                ", catalog='" + catalog + '\'' +
                '}';
    }

    @JsonProperty("name")
    @Override
    public String getName()
    {
        return name;
    }

    @JsonProperty("sqlEngine")
    public SqlEngine getSqlEngine()
    {
        return sqlEngine;
    }

    @JsonProperty("catalog")
    @Nullable
    public String getCatalog()
    {
        return catalog;
    }

    @Override
    public Connection connect()
    {
        return new JdbcConnection(this, sqlEngine.connect());
    }

    @Override
    public Map<String, Set<String>> getSchemaTables()
    {
        try (SqlConnection sqlConnection = sqlEngine.connect()) {
            DatabaseMetaData metaData = sqlConnection.getConnection().getMetaData();

            Map<String, Set<String>> map = new LinkedHashMap<>();
            MetaDataReflection.getTableMetadatas(metaData).forEach(td -> map.computeIfAbsent(td.getTableSchema(), s -> new LinkedHashSet<>()).add(td.getTableName()));
            return map.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> ImmutableSet.copyOf(e.getValue())));
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TableLayout getTableLayout(SchemaTable schemaTable)
    {
        try (SqlConnection sqlConnection = sqlEngine.connect()) {
            DatabaseMetaData metaData = sqlConnection.getConnection().getMetaData();

            TableDescription tableDescription = MetaDataReflection.getTableDescription(
                    metaData, SqlTableIdentifier.of(catalog, schemaTable.getSchema(), schemaTable.getTable()));

            return JdbcLayoutUtils.buildTableLayout(tableDescription);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Scanner newScanner(Table table, Set<String> fields)
    {
        checkArgument(table.getSchema().getConnector() == this);
        return new JdbcScanner(
                table.getSchemaTable(),
                table.getLayout(),
                fields);
    }
}
