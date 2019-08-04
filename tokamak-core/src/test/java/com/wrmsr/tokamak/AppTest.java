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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.driver.BuildContext;
import com.wrmsr.tokamak.driver.BuildNodeVisitor;
import com.wrmsr.tokamak.driver.NodeOutput;
import com.wrmsr.tokamak.driver.Scanner;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.jdbc.JdbcLayoutUtils;
import com.wrmsr.tokamak.jdbc.JdbcTypeUtils;
import com.wrmsr.tokamak.jdbc.JdbcUtils;
import com.wrmsr.tokamak.jdbc.TableIdentifier;
import com.wrmsr.tokamak.jdbc.metadata.MetaDataReflection;
import com.wrmsr.tokamak.jdbc.metadata.TableDescription;
import com.wrmsr.tokamak.jdbc.metadata.TableMetaData;
import com.wrmsr.tokamak.layout.RowLayout;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.type.Type;
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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

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
            ds.forEach(p -> checkState(p.toFile().delete()));
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

            TableDescription td = MetaDataReflection.getTableDescription(
                    metaData, TableIdentifier.of("TEST.DB", "PUBLIC", "NATION"));

            RowLayout rl = new RowLayout(
                    ImmutableList.of("N_NATIONKEY", "N_NAME").stream().collect(toImmutableMap(
                            identity(),
                            f -> JdbcTypeUtils.getTypeForColumn(td.getColumnMetaDatasByName().get(f)))));

            Scanner scanner = new Scanner(
                    "NATION",
                    JdbcLayoutUtils.buildTableLayout(td),
                    ImmutableSet.of(
                            "N_NATIONKEY",
                            "N_NAME"
                    ));

            List<Row> rows = scanner.scan(handle, "N_NATIONKEY", 10);
            System.out.println(rows);

            Node scanNode = new ScanNode(
                    "scan",
                    "NATION",
                    ImmutableMap.of(
                            "N_NATIONKEY", Type.LONG,
                            "N_NAME", Type.STRING
                    ),
                    ImmutableMap.of(),
                    ImmutableMap.of(),
                    ImmutableList.of());

            List<NodeOutput> out = scanNode.accept(
                    new BuildNodeVisitor(),
                    new BuildContext(
                            new DriverContextImpl(handle),
                            Key.of(Id.of(10))));

            handle.commit();

            return null;
        });
    }
}
