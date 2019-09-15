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
package com.wrmsr.tokamak.test;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.catalog.Schema;
import com.wrmsr.tokamak.conn.jdbc.JdbcConnector;
import com.wrmsr.tokamak.sql.SqlEngine;
import com.wrmsr.tokamak.sql.SqlUtils;
import io.airlift.tpch.GenerateUtils;
import io.airlift.tpch.TpchColumn;
import io.airlift.tpch.TpchEntity;
import io.airlift.tpch.TpchTable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.sql.SqlUtils.executeUpdate;

public final class TpchUtils
{
    public static <E extends TpchEntity> Object getColumnValue(TpchColumn column, E entity)
    {
        switch (column.getType().getBase()) {
            case INTEGER:
                return column.getInteger(entity);
            case IDENTIFIER:
                return column.getIdentifier(entity);
            case DATE:
                return GenerateUtils.formatDate(column.getDate(entity));
            case DOUBLE:
                return column.getDouble(entity);
            case VARCHAR:
                return column.getString(entity);
            default:
                throw new IllegalArgumentException(column.getType().toString());
        }
    }

    public static <E extends TpchEntity> void insertEntities(Connection conn, TpchTable<E> table, Iterable<E> entities)
    {
        String stmt = String.format(
                "insert into %s (%s) values (%s)",
                table.getTableName(),
                Joiner.on(", ").join(
                        table.getColumns().stream().map(TpchColumn::getColumnName).collect(toImmutableList())),
                Joiner.on(", ").join(
                        IntStream.range(0, table.getColumns().size()).mapToObj(i -> "?").collect(toImmutableList())));

        try {
            for (E entity : entities) {
                List<Object> f = table.getColumns().stream()
                        .map(c -> getColumnValue(c, entity))
                        .collect(toImmutableList());

                executeUpdate(conn, stmt, f.toArray());
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void buildDatabase(String url)
            throws IOException
    {
        String ddl = CharStreams.toString(new InputStreamReader(TpchUtils.class.getResourceAsStream("tpch_ddl.sql")));

        try (Connection conn = DriverManager.getConnection(url)) {
            for (String stmt : SqlUtils.splitSql(ddl)) {
                executeUpdate(conn, stmt);
            }
            for (TpchTable table : TpchTable.getTables()) {
                TpchUtils.insertEntities(conn, table, table.createGenerator(0.01, 1, 1));
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Catalog buildCatalog(String url)
    {
        Catalog catalog = new Catalog();
        JdbcConnector jdbcConnector = new JdbcConnector("jdbc", new SqlEngine(url));
        Schema schema = catalog.addSchema("PUBLIC", jdbcConnector);
        schema.addTable("NATION");
        schema.addTable("REGION");
        return catalog;
    }
}
