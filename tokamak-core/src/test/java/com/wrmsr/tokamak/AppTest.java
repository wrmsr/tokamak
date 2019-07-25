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
package com.wrmsr.tokamak;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.wrmsr.tokamak.api.FieldName;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.api.TableName;
import com.wrmsr.tokamak.driver.Scanner;
import com.wrmsr.tokamak.jdbc.JdbcUtils;
import com.wrmsr.tokamak.jdbc.TableIdentifier;
import com.wrmsr.tokamak.jdbc.metadata.MetaDataReflection;
import com.wrmsr.tokamak.jdbc.metadata.TableDescription;
import com.wrmsr.tokamak.jdbc.metadata.TableMetaData;
import io.airlift.tpch.TpchTable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdbi.v3.core.Jdbi;

import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class AppTest
        extends TestCase
{
    public AppTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite(AppTest.class);
    }

    public void testJdbcMySql()
            throws Throwable
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://0.0.0.0:21210", "tokamak", "tokamak")) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("select 420")) {
                    while (rs.next()) {
                        System.out.println(rs.getLong(1));
                    }
                }
            }
        }
    }

    public void testJdbcPostgres()
            throws Throwable
    {
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://0.0.0.0:21213/", "tokamak", "tokamak")) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("select 420")) {
                    while (rs.next()) {
                        System.out.println(rs.getLong(1));
                    }
                }
            }
        }
    }

    public void testJdbc2()
            throws Throwable
    {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "root", "tokamak")) {
            assertEquals(JdbcUtils.scalar(conn, "select 420"), 420);
        }
    }

    public void testJdbi()
            throws Throwable
    {
        // DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password");

        Jdbi jdbi = Jdbi.create("jdbc:mysql://0.0.0.0:21211", "root", "tokamak");

        jdbi.withHandle(handle -> {
            handle.execute("create database `tokamak`");
            return null;
        });
    }

    // https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html

    public void testTpch()
            throws Throwable
    {
        String ddl = CharStreams.toString(new InputStreamReader(AppTest.class.getResourceAsStream("tpch_ddl.sql")));

        // String url = "jdbc:h2:mem:test";

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("temp"), "test.db*")) {
            ds.forEach(p -> p.toFile().delete());
        }
        String url = "jdbc:h2:file:./temp/test.db";

        Jdbi jdbi = Jdbi.create(url, "username", "password");
        jdbi.withHandle(handle -> {
            for (String stmt : JdbcUtils.splitSql(ddl)) {
                handle.execute(stmt);
            }

            DatabaseMetaData metaData = handle.getConnection().getMetaData();
            for (TableMetaData tblMd : MetaDataReflection.getTableMetadatas(metaData)) {
                TableDescription td = MetaDataReflection.getTableDescription(metaData, tblMd.getTableIdentifier());
                System.out.println(td);
            }

            for (TpchTable table : TpchTable.getTables()) {
                TpchUtils.insertEntities(handle, table, table.createGenerator(0.01, 1, 1));
            }

            Scanner scanner = new Scanner(
                    TableName.of("nation"),
                    ImmutableSet.of(
                            FieldName.of("n_nationkey"),
                            FieldName.of("n_name")));

            // Codec
            TableDescription td = MetaDataReflection.getTableDescription(metaData, TableIdentifier.of("nation"));
            // td.getCompositePrimaryKeyMetaData().getComponents()

            List<Row> rows = scanner.scan(handle, FieldName.of("n_nationkey"), 10);
            System.out.println(rows);

            handle.commit();

            return null;
        });
    }
}
