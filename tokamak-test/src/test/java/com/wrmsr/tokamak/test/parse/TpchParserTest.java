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
package com.wrmsr.tokamak.test.parse;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.exec.Reflection;
import com.wrmsr.tokamak.core.exec.SimpleExecutable;
import com.wrmsr.tokamak.core.exec.builtin.BuiltinExecutor;
import com.wrmsr.tokamak.core.parse.AstBuilding;
import com.wrmsr.tokamak.core.parse.AstPlanner;
import com.wrmsr.tokamak.core.parse.AstRendering;
import com.wrmsr.tokamak.core.parse.Parsing;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.parse.analysis.ScopeAnalysis;
import com.wrmsr.tokamak.core.parse.transform.SelectExpansion;
import com.wrmsr.tokamak.core.parse.transform.SymbolResolution;
import com.wrmsr.tokamak.core.parse.transform.ViewInlining;
import com.wrmsr.tokamak.core.parse.tree.TreeNode;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.transform.Transforms;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.type.impl.FunctionType;
import com.wrmsr.tokamak.core.util.ApiJson;
import com.wrmsr.tokamak.test.TpchUtils;
import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;

public class TpchParserTest
        extends TestCase
{
    public static String exclaim(String s)
    {
        return s + "!";
    }

    public static Object java(String src, List<Object> args)
    {
        throw new IllegalStateException();
    }

    public void testTpchParse()
            throws Throwable
    {
        Path tempDir = createTempDirectory();
        String url = "jdbc:h2:file:" + tempDir.toString() + "/test.db;USER=username;PASSWORD=password";
        TpchUtils.buildDatabase(url);
        Catalog rootCatalog = TpchUtils.buildCatalog(url);
        Optional<String> defaultSchema = Optional.of("PUBLIC");
        ApiJson.installStatics();

        BuiltinExecutor be = rootCatalog.addExecutor(new BuiltinExecutor("builtin"));
        rootCatalog.addFunction(
                be.register(
                        Reflection.reflect(
                                getClass().getDeclaredMethod("exclaim", String.class), "exclaim")
                ).getName(),
                be);

        // rootCatalog.addFunction(
        //         "java",
        //         be.register()
        //
        // )

        for (String str : new String[] {
                "select * from NATION",
                "select * from NATION, NATION",
                "select N_NAME, N_COMMENT from NATION",
                "select N_NAME as name, N_COMMENT as comment from NATION",
                "select N_NAME, N_COMMENT from NATION as n",
                "select N_COMMENT, exclaim(N_NAME) from NATION",
                "select java('_0 + \"!\"', N_NAME) from NATION",
                // "select N_COMMENT, exclaim(exclaim(N_NAME)) from NATION",
        }) {
            Catalog catalog = new Catalog(ImmutableList.of(rootCatalog));

            SqlParser parser = Parsing.parse(str);
            TreeNode treeNode = AstBuilding.build(parser.statement());
            System.out.println(AstRendering.render(treeNode));

            treeNode = ViewInlining.inlineViews(treeNode, catalog);
            treeNode = SelectExpansion.expandSelects(treeNode, catalog, defaultSchema);
            System.out.println(AstRendering.render(treeNode));

            treeNode = SymbolResolution.resolveSymbols(treeNode, Optional.of(catalog), defaultSchema);
            System.out.println(AstRendering.render(treeNode));

            ScopeAnalysis sa = ScopeAnalysis.analyze(treeNode, Optional.of(catalog), defaultSchema);
            ScopeAnalysis.Resolutions sar = sa.getResolutions();

            Node node = new AstPlanner(Optional.of(catalog), defaultSchema).plan(treeNode);
            Plan plan = new Plan(node);

            plan = Transforms.addScanNodeIdFields(plan, catalog);
            System.out.println(Json.writeValuePretty(plan));

            System.out.println();
        }
    }

    public static final class AnonFunc0
    {
        public static String invoke(String _0)
        {
            return _0 + "!";
        }
    }

    public void testJavaJit()
            throws Throwable
    {
        String stmt = "select java('_0 + \"!\"', N_NAME) from NATION";

        FunctionType funcType = new FunctionType(
                Types.STRING,
                ImmutableList.of(Types.STRING));

        Method method = AnonFunc0.class.getDeclaredMethod("invoke");

        Executable exe = new SimpleExecutable(
                "AnonFunc0",
                funcType,
                args -> {
                    try {
                        return method.invoke(null, args);
                    }
                    catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
