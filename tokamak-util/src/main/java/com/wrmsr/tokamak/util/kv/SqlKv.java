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
package com.wrmsr.tokamak.util.kv;

import com.wrmsr.tokamak.util.sql.SqlConnection;
import com.wrmsr.tokamak.util.sql.SqlEngine;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SqlKv
        implements Kv<byte[], byte[]>
{
    private final SqlEngine engine;
    private final String quote;
    private final String catalog;
    private final String schema;
    private final String table;
    private final String keyColumn;
    private final String valueColumn;

    private final String dst;
    private final String putStmt;
    private final String getStmt;
    private final String deleteStmt;

    public SqlKv(
            SqlEngine engine,
            String quote,
            String catalog,
            String schema,
            String table,
            String keyColumn,
            String valueColumn)
    {
        this.engine = engine;
        this.quote = quote;
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;

        StringBuilder dst = new StringBuilder();
        if (!isNullOrEmpty(catalog)) {
            dst.append(quote(catalog)).append('.');
        }
        if (!isNullOrEmpty(schema)) {
            dst.append(quote(schema)).append('.');
        }
        dst.append(quote(table));
        this.dst = dst.toString();

        putStmt = "merge into " + this.dst + " (" + quote(keyColumn) + ", " + quote(valueColumn) + ") key (" + quote(keyColumn) + ") values (?, ?)";
        getStmt = "select " + quote(valueColumn) + " from " + this.dst + " where " + quote(keyColumn) + " = ?";
        deleteStmt = "delete from " + this.dst + " where " + quote(keyColumn) + " = ?";
    }

    public String getCatalog()
    {
        return catalog;
    }

    public String getSchema()
    {
        return schema;
    }

    public String getTable()
    {
        return table;
    }

    private String quote(String name)
    {
        name = name.replace(quote, quote + quote);
        return quote + name + quote;
    }

    public void createTable()
    {
        String sql = "create table " + dst + " (" + quote(keyColumn) + " binary primary key, " + quote(valueColumn) + " binary)";

        try {
            try (SqlConnection conn = engine.connect();
                    Statement stmt = conn.getConnection().createStatement()) {
                stmt.execute(sql);
                conn.getConnection().commit();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] get(byte[] key)
    {
        try {
            try (SqlConnection conn = engine.connect();
                    PreparedStatement stmt = conn.getConnection().prepareStatement(getStmt)) {
                stmt.setBytes(1, key);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getBytes(1);
                    }
                    else {
                        return null;
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(byte[] key, byte[] value)
    {
        try {
            try (SqlConnection conn = engine.connect();
                    PreparedStatement stmt = conn.getConnection().prepareStatement(putStmt)) {
                stmt.setBytes(1, key);
                stmt.setBytes(2, value);
                stmt.execute();
                conn.getConnection().commit();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(byte[] key)
    {
        try {
            try (SqlConnection conn = engine.connect();
                    PreparedStatement stmt = conn.getConnection().prepareStatement(deleteStmt)) {
                stmt.setBytes(1, key);
                stmt.execute();
                conn.getConnection().commit();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
