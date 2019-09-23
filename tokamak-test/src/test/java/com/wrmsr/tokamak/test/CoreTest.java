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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.plan.node.PersistNode;
import com.wrmsr.tokamak.core.util.ApiJson;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.CatalogRegistry;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.conn.BuiltinConnectors;
import com.wrmsr.tokamak.core.conn.heap.HeapConnector;
import com.wrmsr.tokamak.core.conn.heap.table.ListHeapTable;
import com.wrmsr.tokamak.core.driver.Driver;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.exec.BuiltinExecutors;
import com.wrmsr.tokamak.core.exec.Reflection;
import com.wrmsr.tokamak.core.exec.builtin.BuiltinExecutor;
import com.wrmsr.tokamak.core.layout.RowLayout;
import com.wrmsr.tokamak.core.layout.TableLayout;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.dot.Dot;
import com.wrmsr.tokamak.core.plan.node.EquijoinNode;
import com.wrmsr.tokamak.core.plan.node.FilterNode;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.ProjectNode;
import com.wrmsr.tokamak.core.plan.node.Projection;
import com.wrmsr.tokamak.core.plan.node.ScanNode;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.json.Json;
import com.wrmsr.tokamak.util.sql.SqlUtils;
import junit.framework.TestCase;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;

public class CoreTest
        extends TestCase
{
    @Override
    protected void setUp()
    {
        ApiJson.installStatics();
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

    public void testJdbcMariaDb()
            throws Throwable
    {
        // FIXME: https://docs.oracle.com/javase/8/docs/api/java/sql/DriverManager.html
        Class.forName("org.mariadb.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://0.0.0.0:21215", "tokamak", "tokamak")) {
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
            assertEquals(SqlUtils.executeScalar(conn, "select 420"), 420);
        }
    }

    public static boolean isStringNotNull(String s)
    {
        return s != null;
    }

    public static String addExclamationMark(String s)
    {
        return s + "!";
    }

    private Plan buildPlan(Catalog catalog)
            throws Throwable
    {
        BuiltinExecutor be = catalog.addExecutor(new BuiltinExecutor("builtin"));

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
                catalog.addFunction(be.register(Reflection.reflect(getClass().getDeclaredMethod("isStringNotNull", String.class))).getName(), be).asNodeFunction(),
                ImmutableList.of("N_NAME"),
                false);

        Node projectNode0 = new ProjectNode(
                "project0",
                filterNode0,
                Projection.of(
                        "N_NATIONKEY", "N_NATIONKEY",
                        "N_NAME", catalog.addFunction(be.register(Reflection.reflect(getClass().getDeclaredMethod("addExclamationMark", String.class))).getName(), be), "N_NAME",
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

        Node persistNode0 = new PersistNode(
                "persist0",
                equijoinNode0,
                ImmutableList.of(),
                false,
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        return new Plan(persistNode0);
    }

    // https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html

    public void testTpch()
            throws Throwable
    {
        Path tempDir = createTempDirectory();
        String url = "jdbc:h2:file:" + tempDir.toString() + "/test.db;USER=username;PASSWORD=password";
        TpchUtils.buildDatabase(url);
        Catalog catalog = TpchUtils.buildCatalog(url);

        CatalogRegistry cn = new CatalogRegistry();
        BuiltinConnectors.register(cn);
        BuiltinExecutors.register(cn);
        ObjectMapper om = cn.register(Json.newObjectMapper());
        String src = om.writerWithDefaultPrettyPrinter().writeValueAsString(catalog);
        System.out.println(src);
        // catalog = om.readValue(src, Catalog.class);

        Plan plan = buildPlan(catalog);

        src = om.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
        System.out.println(src);
        // plan = om.readValue(src, Plan.class);

        Driver driver = new DriverImpl(catalog, plan);

        Driver.Context ctx = driver.createContext();
        Collection<Row> buildRows = driver.build(
                ctx,
                plan.getRoot(),
                Key.of("N_NATIONKEY", 10));

        System.out.println(buildRows);

        // driver.sync(ctx, )

        ctx.commit();
    }

    public void testDot()
            throws Throwable
    {
        Catalog catalog = new Catalog();

        Plan plan = buildPlan(catalog);

        Dot.openDot(Dot.buildPlanDot(plan));
    }

    public void testHeapTable()
            throws Throwable
    {
        ListHeapTable listHeapTable = new ListHeapTable(
                SchemaTable.of("stuff_schema", "stuff_table"),
                new TableLayout(
                        new RowLayout(ImmutableMap.of(
                                "id", Type.LONG,
                                "str", Type.STRING
                        )),
                        new TableLayout.Key(ImmutableList.of("id")),
                        ImmutableList.of()));

        listHeapTable.addRowMaps(ImmutableList.of(
                ImmutableMap.of(
                        "id", 1,
                        "str", "one"
                ),
                ImmutableMap.of(
                        "id", 2,
                        "str", "two"
                )
        ));

        HeapConnector connector = new HeapConnector("stuff_connector");
        connector.addTable(listHeapTable);

        Catalog catalog = new Catalog();
        Table table = catalog.addSchema("stuff_schema", connector).addTable("stuff_table");

        CatalogRegistry cn = BuiltinConnectors.register(new CatalogRegistry());
        ObjectMapper om = cn.register(Json.newObjectMapper());
        cn.checkRegistered(om);

        String src = om.writerWithDefaultPrettyPrinter().writeValueAsString(catalog);
        System.out.println(src);
        // catalog = om.readValue(src, Catalog.class);

        ScanNode scan0 = new ScanNode(
                "scan0",
                SchemaTable.of("stuff_schema", "stuff_table"),
                ImmutableMap.of(
                        "id", Type.LONG,
                        "str", Type.STRING
                ),
                ImmutableSet.of("id"),
                ImmutableSet.of(),
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        Plan plan = new Plan(scan0);

        Driver driver = new DriverImpl(catalog, plan);

        Collection<Row> buildRows = driver.build(
                driver.createContext(),
                plan.getRoot(),
                Key.of("id", 1));

        System.out.println(buildRows);
    }
}
