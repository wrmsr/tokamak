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
import com.google.common.collect.ImmutableSet;
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
import com.wrmsr.tokamak.core.parse.analysis.TypeAnalysis;
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
import com.wrmsr.tokamak.util.Jdk;
import com.wrmsr.tokamak.util.java.compile.javac.InProcJavaCompiler;
import com.wrmsr.tokamak.util.java.lang.JAccess;
import com.wrmsr.tokamak.util.java.lang.JName;
import com.wrmsr.tokamak.util.java.lang.JParam;
import com.wrmsr.tokamak.util.java.lang.JRenderer;
import com.wrmsr.tokamak.util.java.lang.JTypeSpecifier;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JDeclaration;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JMethod;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JType;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JRawExpression;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JReturn;
import com.wrmsr.tokamak.util.java.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.util.java.lang.unit.JPackageSpec;
import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Sets.immutableEnumSet;
import static com.wrmsr.tokamak.util.MoreCollections.enumerate;
import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;
import static com.wrmsr.tokamak.util.java.lang.tree.JTrees.jblockify;

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

            TypeAnalysis ta = TypeAnalysis.analyze(treeNode, catalog, defaultSchema);

            Node node = new AstPlanner(Optional.of(catalog), defaultSchema).plan(treeNode);
            Plan plan = new Plan(node);

            plan = Transforms.addScanNodeIdFields(plan, catalog);
            System.out.println(Json.writeValuePretty(plan));

            System.out.println();
        }
    }

    public static Method jitJavaExpr(String src, Type retType, List<Type> argTypes)
            throws Throwable
    {
        String bareName = "AnonFunc0";

        JCompilationUnit jcu = new JCompilationUnit(
                Optional.of(new JPackageSpec(JName.of("com", "wrmsr", "tokamak", "generated"))),
                ImmutableSet.of(),
                new JType(
                        immutableEnumSet(JAccess.PUBLIC, JAccess.FINAL),
                        JType.Kind.CLASS,
                        bareName,
                        ImmutableList.of(),
                        ImmutableList.<JDeclaration>of(
                                new JMethod(
                                        immutableEnumSet(JAccess.PUBLIC, JAccess.STATIC),
                                        JTypeSpecifier.of(retType.getReflect()),
                                        "invoke",
                                        enumerate(argTypes.stream())
                                                .map(a -> new JParam(
                                                        JTypeSpecifier.of(a.getItem().getReflect()),
                                                        "_" + a.getIndex()))
                                                .collect(toImmutableList()),
                                        Optional.of(
                                                jblockify(
                                                        new JReturn(
                                                                new JRawExpression(src))))))));

        String jsrc = JRenderer.renderWithIndent(jcu, "    ");

        Class<?> cls = InProcJavaCompiler.compileAndLoad(
                jsrc,
                "com.wrmsr.tokamak.generated." + bareName,
                bareName,
                ImmutableList.of(
                        "-source", "1.8",
                        "-target", "1.8",
                        "-classpath", Jdk.getClasspath()
                ),
                TpchParserTest.class.getClassLoader());

        Method method = cls.getDeclaredMethod(
                "invoke", argTypes.stream().map(Type::getReflect).collect(toImmutableList()).toArray(new Class<?>[] {}));

        return method;
    }

    public void testJavaJit()
            throws Throwable
    {
        String src = "_0 + \"!\"";
        String stmt = "select java('" + src + "', N_NAME) from NATION";

        Method method = jitJavaExpr(src, Types.STRING, ImmutableList.of(Types.STRING));

        FunctionType funcType = new FunctionType(
                Types.STRING,
                ImmutableList.of(Types.STRING));

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
