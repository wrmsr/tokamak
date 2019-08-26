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
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.catalog.Schema;
import com.wrmsr.tokamak.driver.Driver;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.func.RowViewFunction;
import com.wrmsr.tokamak.jdbc.JdbcConnector;
import com.wrmsr.tokamak.jdbc.JdbcUtils;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.Projection;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.plan.dot.Dot;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.type.Type;
import io.airlift.tpch.TpchTable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

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

    private void clearDatabase()
            throws IOException
    {
        // String url = "jdbc:h2:mem:test";
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("temp"), "test.db*")) {
            ds.forEach(p -> checkState(p.toFile().delete()));
        }
    }

    private void buildDatabase(String url)
            throws IOException
    {
        String ddl = CharStreams.toString(new InputStreamReader(AppTest.class.getResourceAsStream("tpch_ddl.sql")));

        Jdbi jdbi = Jdbi.create(url);
        jdbi.withHandle(handle -> {
            for (String stmt : JdbcUtils.splitSql(ddl)) {
                handle.execute(stmt);
            }
            for (TpchTable table : TpchTable.getTables()) {
                TpchUtils.insertEntities(handle, table, table.createGenerator(0.01, 1, 1));
            }
            handle.commit();
            return null;
        });
    }

    private Catalog buildCatalog(String url)
    {
        Catalog catalog = new Catalog();
        JdbcConnector jdbcConnector = new JdbcConnector("jdbc", url);
        Schema schema = catalog.getOrBuildSchema("PUBLIC", jdbcConnector);
        schema.getOrBuildTable("NATION");
        schema.getOrBuildTable("REGION");
        return catalog;
    }

    private Plan buildPlan(Catalog catalog)
    {
        Node scanNode0 = new ScanNode(
                "scan0",
                SchemaTable.of("PUBLIC", "NATION"),
                ImmutableMap.of(
                        "N_NATIONKEY", Type.LONG,
                        "N_NAME", Type.STRING,
                        "N_REGIONKEY", Type.LONG
                ),
                ImmutableSet.of("N_NATIONKEY"),
                ImmutableSet.of(),
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        Node filterNode0 = new FilterNode(
                "filter0",
                scanNode0,
                row -> row.getAttributes()[0] != null,
                false);

        Node projectNode0 = new ProjectNode(
                "project0",
                filterNode0,
                Projection.of(
                        "N_NATIONKEY", "N_NATIONKEY",
                        "N_NAME", catalog.addFunction(RowViewFunction.anon(Type.STRING, rv -> rv.get("N_NAME") + "!")),
                        "N_REGIONKEY", "N_REGIONKEY"
                ));

        Node scanNode1 = new ScanNode(
                "scan1",
                SchemaTable.of("PUBLIC", "REGION"),
                ImmutableMap.of(
                        "R_REGIONKEY", Type.LONG,
                        "R_NAME", Type.STRING
                ),
                ImmutableSet.of("R_REGIONKEY"),
                ImmutableSet.of(),
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        Node equijoinNode0 = new EquijoinNode(
                "equijoin0",
                ImmutableList.of(
                        new EquijoinNode.Branch(projectNode0, ImmutableList.of("N_REGIONKEY")),
                        new EquijoinNode.Branch(scanNode1, ImmutableList.of("R_REGIONKEY"))
                ),
                EquijoinNode.Mode.INNER);

        return new Plan(equijoinNode0);
    }

    public void testTpch()
            throws Throwable
    {
        clearDatabase();

        String url = "jdbc:h2:file:./temp/test.db;USER=username;PASSWORD=password";

        buildDatabase(url);

        Catalog catalog = buildCatalog(url);

        Plan plan = buildPlan(catalog);

        Driver driver = new DriverImpl(catalog, plan);

        Collection<Row> buildRows = driver.build(
                driver.createContext(),
                plan.getRoot(),
                Key.of("N_NATIONKEY", 10));
        System.out.println(buildRows);
    }

    public void testDot()
            throws Throwable
    {
        Catalog catalog = new Catalog();

        Plan plan = buildPlan(catalog);

        Dot.openDot(Dot.buildPlanDot(plan));
    }
}
