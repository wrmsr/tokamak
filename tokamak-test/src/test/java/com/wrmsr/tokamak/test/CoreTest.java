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
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Catalog;
import com.wrmsr.tokamak.catalog.ConnectorRegistry;
import com.wrmsr.tokamak.catalog.Table;
import com.wrmsr.tokamak.conn.BuiltinConnectors;
import com.wrmsr.tokamak.conn.heap.HeapConnector;
import com.wrmsr.tokamak.conn.heap.table.MapHeapTable;
import com.wrmsr.tokamak.driver.Driver;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.func.RowMapFunction;
import com.wrmsr.tokamak.layout.RowLayout;
import com.wrmsr.tokamak.layout.TableLayout;
import com.wrmsr.tokamak.node.EquijoinNode;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.Projection;
import com.wrmsr.tokamak.node.ScanNode;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.plan.dot.Dot;
import com.wrmsr.tokamak.sql.SqlUtils;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.Json;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

public class CoreTest
        extends TestCase
{

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
            assertEquals(SqlUtils.executeScalar(conn, "select 420"), 420);
        }
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
                        "N_NAME", catalog.addFunction(RowMapFunction.anon(Type.STRING, rv -> rv.get("N_NAME") + "!")),
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

    // https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html

    public void testTpch()
            throws Throwable
    {
        TpchUtils.clearDatabase();
        String url = "jdbc:h2:file:./temp/test.db;USER=username;PASSWORD=password";
        TpchUtils.buildDatabase(url);
        Catalog catalog = TpchUtils.buildCatalog(url);

        ConnectorRegistry cn = BuiltinConnectors.register(new ConnectorRegistry());
        ObjectMapper om = cn.registerSubtypes(Json.newObjectMapper());
        String src = om.writerWithDefaultPrettyPrinter().writeValueAsString(catalog);
        System.out.println(src);
        catalog = om.readValue(src, Catalog.class);

        Plan plan = buildPlan(catalog);

        // FIXME: projectnode lambdas + filternode predicate
        // src = om.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
        // System.out.println(src);
        // plan = om.readValue(src, Plan.class);

        Driver driver = new DriverImpl(catalog, plan);

        Driver.Context driverContext = driver.createContext();
        Collection<Row> buildRows = driver.build(
                driverContext,
                plan.getRoot(),
                Key.of("N_NATIONKEY", 10));

        System.out.println(buildRows);

        driverContext.commit();
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
        MapHeapTable mapHeapTable = new MapHeapTable(
                SchemaTable.of("stuff_schema", "stuff_table"),
                new TableLayout(
                        new RowLayout(ImmutableMap.of(
                                "id", Type.LONG,
                                "str", Type.STRING
                        )),
                        new TableLayout.Key(ImmutableList.of("id")),
                        ImmutableList.of()));

        mapHeapTable.addRowMaps(ImmutableList.of(
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
        connector.addTable(mapHeapTable);

        Catalog catalog = new Catalog();
        Table table = catalog.getOrBuildSchema("stuff_schema", connector).getOrBuildTable("stuff_table");

        ConnectorRegistry cn = BuiltinConnectors.register(new ConnectorRegistry());
        ObjectMapper om = cn.registerSubtypes(Json.newObjectMapper());
        cn.checkConnectorSubtypeRegistered(om);

        String src = om.writerWithDefaultPrettyPrinter().writeValueAsString(catalog);
        System.out.println(src);
        catalog = om.readValue(src, Catalog.class);

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
