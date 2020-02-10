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
import com.wrmsr.tokamak.core.exec.builtin.BuiltinFunctions;
import com.wrmsr.tokamak.core.layout.RowLayout;
import com.wrmsr.tokamak.core.layout.TableLayout;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.PlanningContext;
import com.wrmsr.tokamak.core.plan.analysis.id.IdAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.id.part.IdAnalysisPart;
import com.wrmsr.tokamak.core.plan.analysis.origin.OriginAnalysis;
import com.wrmsr.tokamak.core.plan.analysis.origin.Origination;
import com.wrmsr.tokamak.core.plan.dot.PlanDot;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PFunction;
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.transform.PropagateIdsTransform;
import com.wrmsr.tokamak.core.plan.transform.SetInvalidationsTransform;
import com.wrmsr.tokamak.core.plan.value.VNode;
import com.wrmsr.tokamak.core.plan.value.VNodes;
import com.wrmsr.tokamak.core.shell.ShellSession;
import com.wrmsr.tokamak.core.shell.TokamakShell;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.util.ApiJson;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.core.util.dot.Dot;
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
import java.util.function.Consumer;

import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;
import static java.util.function.Function.identity;

public class CoreTest
        extends TestCase
{
    private static final boolean DOT =
            // true;
            false;

    @Override
    protected void setUp()
    {
        ApiJson.installStatics();
    }

    public void testJdbcMySql()
            throws Throwable
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://0.0.0.0:21215", "tokamak", "tokamak")) {
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
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://0.0.0.0:21217", "tokamak", "tokamak")) {
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
        BuiltinExecutor be = (BuiltinExecutor) catalog.getExecutorsByName().get("builtin");

        PNode scanNode0 = new PScan(
                "scan0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                SchemaTable.of("PUBLIC", "NATION"),
                ImmutableMap.<String, Type>builder()
                        // .put("N_NATIONKEY", Types.LONG)
                        .put("N_NAME", Types.String())
                        .put("N_REGIONKEY", Types.Long())
                        .put("N_COMMENT", Types.String())
                        .build(),
                PInvalidations.empty());

        PNode scanNode0Dropped = new PProject(
                "scanNode0Dropped",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                scanNode0,
                PProjection.only(/*"N_NATIONKEY",*/ "N_NAME", "N_REGIONKEY"));

        PNode stateNode0 = new PState(
                "state0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                scanNode0Dropped,
                PState.Denormalization.NONE,
                PInvalidations.empty());

        PNode filterPrjNode0 = new PProject(
                "filterPrjNode0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                stateNode0,
                new PProjection(ImmutableMap.<String, VNode>builder()
                        .putAll(stateNode0.getFields().getNames().stream().collect(toImmutableMap(identity(), VNodes::field)))
                        .put("isStringNotNull", VNodes.function(
                                catalog.addFunction(be.register(Reflection.reflect(getClass().getDeclaredMethod("isStringNotNull", String.class))).getName(), be).asNodeFunction(),
                                VNodes.field("N_NAME")))
                        .build()));

        PNode filterNode0 = new PFilter(
                "filter0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                filterPrjNode0,
                "isStringNotNull",
                PFilter.Linking.LINKED);

        PNode droppedFilterNode0 = new PProject(
                "droppedFilterNode0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                filterNode0,
                PProjection.only(stateNode0.getFields().getNames()));

        com.wrmsr.tokamak.core.catalog.Function func = catalog.addFunction(
                be.register(Reflection.reflect(getClass().getDeclaredMethod("addExclamationMark", String.class))).getName(), be);

        PNode projectNode0 = new PProject(
                "project0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                droppedFilterNode0,
                new PProjection(ImmutableMap.of(
                        // "N_NATIONKEY", PValue.field("N_NATIONKEY"),
                        "N_NAME", VNodes.function(
                                PFunction.of(func.getExecutable()),
                                VNodes.field("N_NAME")),
                        // "N_REGIONKEY", PValue.field("N_REGIONKEY")
                        "N_REGIONKEY", VNodes.function(
                                PFunction.of(be.getExecutable("transmuteInternal")),
                                VNodes.field("N_REGIONKEY"))
                )));

        PNode scanNode1 = new PScan(
                "scan1",
                AnnotationCollection.of(),
                AnnotationCollectionMap.copyOf(ImmutableMap.of("R_REGIONKEY", AnnotationCollection.of(FieldAnnotation.id()))),
                SchemaTable.of("PUBLIC", "REGION"),
                ImmutableMap.of(
                        "R_REGIONKEY", Types.Long(),
                        "R_NAME", Types.String()
                ),
                PInvalidations.empty());

        PNode stateNode1 = new PState(
                "state1",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                scanNode1,
                PState.Denormalization.NONE,
                PInvalidations.empty());

        PNode equiJoinNode0 = new PJoin(
                "equiJoin0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                ImmutableList.of(
                        new PJoin.Branch(projectNode0, ImmutableList.of("N_REGIONKEY")),
                        new PJoin.Branch(stateNode1, ImmutableList.of("R_REGIONKEY"))
                ),
                PJoin.Mode.INNER);

        PNode persistNode0 = new PState(
                "state2",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                equiJoinNode0,
                PState.Denormalization.NONE,
                PInvalidations.empty());

        PNode outputNode0 = new POutput(
                "output0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                persistNode0,
                ImmutableList.of());

        return Plan.of(outputNode0);
    }

    // https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html

    public void testTpch()
            throws Throwable
    {
        Path tempDir = createTempDirectory();
        String url = "jdbc:h2:file:" + tempDir.toString() + "/test.db;USER=username;PASSWORD=password";
        TpchUtils.buildDatabase(url);
        Catalog catalog = TpchUtils.buildCatalog(url);

        BuiltinExecutor be = catalog.addExecutor(new BuiltinExecutor("builtin"));
        BuiltinFunctions.register(be);
        ((Consumer<Catalog>) (c -> be.getExecutablesByName().keySet().forEach(n -> c.addFunction(n, be)))).accept(catalog);

        CatalogRegistry cn = new CatalogRegistry();
        BuiltinConnectors.register(cn);
        BuiltinExecutors.register(cn);

        ObjectMapper om = Json.newObjectMapper();
        cn.register(om);
        Types.BUILTIN_REGISTRY.registerDeserializers(om);

        String src = om.writerWithDefaultPrettyPrinter().writeValueAsString(catalog);
        System.out.println(src);

        om.readValue("null", Type.class);
        // catalog = om.readValue(src, Catalog.class);

        Plan plan = buildPlan(catalog);
        // Dot.open(PlanDot.build(plan));

        // plan = SetIdFieldsTransform.setIdFields(plan, Optional.of(catalog));
        // Dot.openDot(Dot.buildPlanDot(plan));

        plan = PropagateIdsTransform.propagateIds(plan, new PlanningContext(Optional.of(catalog), Optional.empty()));
        // Dot.open(PlanDot.build(plan));

        plan = SetInvalidationsTransform.setInvalidations(plan, new PlanningContext(Optional.of(catalog), Optional.empty()));
        if (DOT) { Dot.open(PlanDot.build(plan)); }

        OriginAnalysis oa = OriginAnalysis.analyze(plan);
        oa.getLeafChainAnalysis().getSinkSetsByFirstSource();
        oa.getStateChainAnalysis().getSinkSetsByFirstSource();

        IdAnalysis ifa = IdAnalysis.analyze(plan, Optional.of(catalog));
        System.out.println(ifa);

        PState state2 = (PState) plan.getNode("state2");
        for (IdAnalysisPart part : ifa.get(state2).getParts()) {
            for (String field : part) {
                for (Origination o : oa.getStateChainAnalysis().getFirstOriginationSetsBySink().get(PNodeField.of(state2, field))) {
                    oa.getOriginationSetsBySink().get(o.getSource().get());
                    oa.getStateChainAnalysis().getFirstOriginationSetsBySink().get(o.getSource().get());
                }
            }
        }

        src = om.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
        System.out.println(src);
        // plan = om.readValue(src, Plan.class);

        Driver driver = new DriverImpl(catalog, plan);

        Driver.Context ctx = driver.newContext();
        Collection<Row> buildRows = driver.build(
                ctx,
                plan.getRoot(),
                Key.of("R_REGIONKEY", 1));

        System.out.println(buildRows);

        // driver.sync(ctx, )

        ctx.commit();

        System.out.println(ctx);

        ctx = driver.newContext();
        buildRows = driver.build(
                ctx,
                plan.getRoot(),
                Key.of("R_REGIONKEY", 1));

        System.out.println(buildRows);

        ctx.commit();

        System.out.println(ctx);

        // String stmtStr = "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION inner join REGION on N_REGIONKEY = R_REGIONKEY";
        // SqlParser parser = TreeParsing.parse(stmtStr);
        // TNode treeNode = TreeParsing.build(parser.statement());
        // System.out.println(TreeRendering.render(treeNode));
    }

    public void testShell()
            throws Throwable
    {
        Path tempDir = createTempDirectory();
        String url = "jdbc:h2:file:" + tempDir.toString() + "/test.db;USER=username;PASSWORD=password";
        TpchUtils.buildDatabase(url);

        TokamakShell shell = new TokamakShell();
        TpchUtils.setupCatalog(shell.getRootCatalog(), url);

        for (String sql : new String[]{
                "select N_NAME, N_COMMENT from NATION where N_REGIONKEY = 1",

                "select N_NAME, N_REGIONKEY, N_COMMENT from NATION",

                "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION, REGION where N_REGIONKEY = R_REGIONKEY",

                "select L_QUANTITY, O_ORDERKEY, O_ORDERSTATUS, P_NAME, S_NAME " +
                        "from LINEITEM, ORDERS, PART, SUPPLIER where L_ORDERKEY = O_ORDERKEY and L_PARTKEY = P_PARTKEY and L_SUPPKEY = S_SUPPKEY",
        }) {
            System.out.println(sql);

            ShellSession session = shell.newSession()
                    .setDefaultSchema(Optional.of("PUBLIC"));
            Plan plan = shell.plan(sql, session);
            if (DOT) { Dot.open(PlanDot.build(plan)); }

            Driver driver = new DriverImpl(session.getCatalog(), plan);

            for (int i = 1; i < 4; ++i) {
                Driver.Context ctx = driver.newContext();
                Collection<Row> buildRows = driver.build(
                        ctx,
                        plan.getRoot(),
                        Key.of(sql.contains("N_REGIONKEY") ? "N_REGIONKEY" : "O_ORDERKEY", i));
                buildRows.forEach(System.out::println);

                ctx.commit();
                System.out.println(ctx);
                System.out.println();
            }
        }
    }

    public void testHeapTable()
            throws Throwable
    {
        ListHeapTable listHeapTable = new ListHeapTable(
                SchemaTable.of("stuff_schema", "stuff_table"),
                new TableLayout(
                        new RowLayout(FieldCollection.of(ImmutableMap.of(
                                "id", Types.Long(),
                                "str", Types.String()
                        ))),
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

        PScan scan0 = new PScan(
                "scan0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.copyOf(ImmutableMap.of("id", AnnotationCollection.of(FieldAnnotation.id()))),
                SchemaTable.of("stuff_schema", "stuff_table"),
                ImmutableMap.of(
                        "id", Types.Long(),
                        "str", Types.String()
                ),
                PInvalidations.empty());

        Plan plan = Plan.of(scan0);

        Driver driver = new DriverImpl(catalog, plan);

        Collection<Row> buildRows = driver.build(
                driver.newContext(),
                plan.getRoot(),
                Key.of("id", 1));

        System.out.println(buildRows);
    }
}
