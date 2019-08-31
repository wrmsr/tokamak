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
package com.wrmsr.tokamak.conn.jdbc;

import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Connector;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.layout.TableLayout;
import com.wrmsr.tokamak.sql.SqlConnection;
import com.wrmsr.tokamak.sql.SqlEngine;
import com.wrmsr.tokamak.sql.metadata.MetaDataReflection;
import com.wrmsr.tokamak.sql.metadata.TableDescription;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class JdbcConnector
        implements Connector
{
    private final String name;
    private final SqlEngine sqlEngine;

    public JdbcConnector(String name, SqlEngine sqlEngine)
    {
        this.name = checkNotEmpty(name);
        this.sqlEngine = checkNotNull(sqlEngine);
    }

    @Override
    public String toString()
    {
        return "JdbcConnector{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Connection connect()
    {
        return new JdbcConnection(this, sqlEngine.connect());
    }

    @Override
    public TableLayout getTableLayout(SchemaTable schemaTable)
    {
        try (SqlConnection sqlConnection = sqlEngine.connect()) {
            DatabaseMetaData metaData = sqlConnection.getConnection().getMetaData();

            TableDescription tableDescription = MetaDataReflection.getTableDescription(
                    metaData, JdbcTableIdentifier.of("TEST.DB", "PUBLIC", schemaTable.getTable()));

            return JdbcLayoutUtils.buildTableLayout(tableDescription);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Scanner createScanner(Table table, Set<String> fields)
    {
        checkArgument(table.getSchema().getConnector() == this);
        return new JdbcScanner(
                table.getSchemaTable(),
                table.getLayout(),
                fields);
    }
}
