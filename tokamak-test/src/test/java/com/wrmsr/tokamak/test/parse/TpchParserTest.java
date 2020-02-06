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
import com.wrmsr.tokamak.core.exec.builtin.BuiltinFunctions;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.dot.PlanDot;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.transform.DropExposedInternalFieldsTransform;
import com.wrmsr.tokamak.core.plan.transform.MergeScansTransform;
import com.wrmsr.tokamak.core.plan.transform.PersistExposedTransform;
import com.wrmsr.tokamak.core.plan.transform.PersistScansTransform;
import com.wrmsr.tokamak.core.plan.transform.PropagateIdsTransform;
import com.wrmsr.tokamak.core.plan.transform.SetInvalidationsTransform;
import com.wrmsr.tokamak.core.tree.ParseOptions;
import com.wrmsr.tokamak.core.tree.ParsingContext;
import com.wrmsr.tokamak.core.tree.TreeParsing;
import com.wrmsr.tokamak.core.tree.TreeRendering;
import com.wrmsr.tokamak.core.tree.analysis.SymbolAnalysis;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.plan.TreePlanner;
import com.wrmsr.tokamak.core.tree.transform.SelectExpansion;
import com.wrmsr.tokamak.core.tree.transform.SymbolResolution;
import com.wrmsr.tokamak.core.tree.transform.ViewInlining;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;
import com.wrmsr.tokamak.core.util.ApiJson;
import com.wrmsr.tokamak.core.util.dot.Dot;
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
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Sets.immutableEnumSet;
import static com.wrmsr.tokamak.util.MoreCollections.enumerate;
import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;
import static com.wrmsr.tokamak.util.java.lang.tree.JTrees.jblockify;

public class TpchParserTest
        extends TestCase
{
    private static final boolean DOT =
            true;
            // false;

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
        BuiltinFunctions.register(be);
        ((Consumer<Catalog>) (c -> be.getExecutablesByName().keySet().forEach(n -> c.addFunction(n, be)))).accept(rootCatalog);

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

        // FIXME: ensure whole string consumed :|

        for (String str : new String[] {
                // "select * where from",

                "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION, REGION where N_REGIONKEY = R_REGIONKEY and N_NATIONKEY = 420",
                "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION, REGION where N_REGIONKEY = R_REGIONKEY",

                // "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION, REGION where N_REGIONKEY = R_REGIONKEY and N_NATIONKEY = R_REGIONKEY",

                "select * from NATION",
                "select * from NATION, NATION",
                "select N_NAME, N_COMMENT from NATION",
                "select N_NAME as name, N_COMMENT as comment from NATION",
                "select N_NAME, N_COMMENT from NATION as n",
                "select N_COMMENT, exclaim(N_NAME) from NATION",
                "select N_NATIONKEY, R_REGIONKEY from NATION, REGION",
                "select N_NATIONKEY, R_REGIONKEY, NATION.N_COMMENT from NATION, REGION",
                "select N2.N_NATIONKEY, R_REGIONKEY, NATION.N_COMMENT from NATION, REGION, NATION as N2",
                "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION, REGION where N_REGIONKEY = R_REGIONKEY",

                // "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION join REGION on N_REGIONKEY = R_REGIONKEY",
                // "select N_NAME, N_REGIONKEY, N_COMMENT, R_NAME from NATION inner join REGION on N_REGIONKEY = R_REGIONKEY",
                // "select java('String', '_0 + \"!\"', N_NAME) from NATION",
                // "select N_COMMENT, exclaim(exclaim(N_NAME)) from NATION",
        }) {
            Catalog catalog = new Catalog(ImmutableList.of(rootCatalog));

            SqlParser parser = TreeParsing.parse(str);
            TNode treeNode = TreeParsing.build(parser.statement());
            System.out.println(TreeRendering.render(treeNode));

            ParsingContext parsingContext = new ParsingContext(
                    new ParseOptions(),
                    Optional.of(catalog),
                    defaultSchema);

            treeNode = ViewInlining.inlineViews(treeNode, catalog);
            treeNode = SelectExpansion.expandSelects(treeNode, parsingContext);
            System.out.println(TreeRendering.render(treeNode));

            treeNode = SymbolResolution.resolveSymbols(treeNode, parsingContext);
            System.out.println(TreeRendering.render(treeNode));

            SymbolAnalysis sa = SymbolAnalysis.analyze(treeNode, parsingContext);
            SymbolAnalysis.Resolutions sar = sa.getResolutions();

            PNode node = new TreePlanner(parsingContext).plan(treeNode);
            Plan plan = Plan.of(node);
            if (DOT) { Dot.open(PlanDot.build(plan)); }

            plan = MergeScansTransform.mergeScans(plan);
            plan = PersistScansTransform.persistScans(plan);
            plan = PersistExposedTransform.persistExposed(plan);
            if (DOT) { Dot.open(PlanDot.build(plan)); }

            plan = PropagateIdsTransform.propagateIds(plan, Optional.of(catalog));
            plan = DropExposedInternalFieldsTransform.dropExposedInternalFields(plan, Optional.of(catalog));
            plan = SetInvalidationsTransform.setInvalidations(plan, Optional.of(catalog));
            if (DOT) { Dot.open(PlanDot.build(plan)); }

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
                                        JTypeSpecifier.of(retType.toReflect().get()),
                                        "invoke",
                                        enumerate(argTypes.stream())
                                                .map(a -> new JParam(
                                                        JTypeSpecifier.of(a.getItem().toReflect().get()),
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
                "invoke", argTypes.stream().map(Type::toReflect).collect(toImmutableList()).toArray(new Class<?>[] {}));

        return method;
    }

    public void testJavaJit()
            throws Throwable
    {
        String src = "_0 + \"!\"";
        String stmt = "select java('String', '" + src + "', N_NAME) from NATION";

        Method method = jitJavaExpr(src, Types.String(), ImmutableList.of(Types.String()));

        FunctionType funcType = new FunctionType(
                Types.String(),
                ImmutableList.of(Types.String()));

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
