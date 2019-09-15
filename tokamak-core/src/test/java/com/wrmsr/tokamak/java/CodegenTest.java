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
package com.wrmsr.tokamak.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.java.compile.javac.InProcJavaCompiler;
import com.wrmsr.tokamak.java.lang.JAccess;
import com.wrmsr.tokamak.java.lang.JName;
import com.wrmsr.tokamak.java.lang.JRenderer;
import com.wrmsr.tokamak.java.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.java.lang.tree.declaration.JType;
import com.wrmsr.tokamak.java.lang.tree.expression.JLiteral;
import com.wrmsr.tokamak.java.lang.tree.expression.JMethodInvocation;
import com.wrmsr.tokamak.java.lang.tree.statement.JExpressionStatement;
import com.wrmsr.tokamak.java.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.java.lang.unit.JPackageSpec;
import junit.framework.TestCase;

import java.util.Optional;

import static com.google.common.collect.Sets.immutableEnumSet;
import static com.wrmsr.tokamak.java.lang.tree.JTrees.jblockify;

public class CodegenTest
        extends TestCase
{
    /*
    TODO:
     - struct / box
     - final subclassing
      - ctor forwarding
       - factory exposing
     - intrusive collections (ghetto valhalla)
      - easier w bytecode? or on src at boot?
       - https://wiki.openjdk.java.net/display/valhalla/Minimal+Value+Types
     - parsing?
    */

    public void testCodegen()
            throws Throwable
    {
        JCompilationUnit cu = new JCompilationUnit(
                Optional.of(new JPackageSpec(JName.of("com", "wrmsr", "tokamak"))),
                ImmutableSet.of(),
                new JType(
                        immutableEnumSet(JAccess.PUBLIC, JAccess.FINAL),
                        JType.Kind.CLASS,
                        "Thing",
                        ImmutableList.of(),
                        ImmutableList.of(
                                new JConstructor(
                                        immutableEnumSet(JAccess.PUBLIC),
                                        "Thing",
                                        ImmutableList.of(),
                                        jblockify(
                                                new JExpressionStatement(
                                                        JMethodInvocation.of(
                                                                JName.of("System", "out", "println"),
                                                                ImmutableList.of(
                                                                        new JLiteral("hi")
                                                                )))))
                        )));

        String rendered = JRenderer.renderWithIndent(cu, "    ");
        System.out.println(rendered);

        Class<?> cls = InProcJavaCompiler.compileAndLoad(
                rendered,
                "com.wrmsr.tokamak.Thing",
                "Thing",
                ImmutableList.of(),
                null);
        Object obj = cls.getDeclaredConstructor().newInstance();
        // ((Runnable) obj).run();
        System.out.println(obj);
    }

    public static class BaseThing
    {
        public int x()
        {
            return 420;
        }
    }

    public void testClasspath()
            throws Throwable
    {
        // ClassLoader cl0 = ClassLoader.getSystemClassLoader();
        // ClassLoader cl1 = InProcJavaCompiler.class.getClassLoader();
        String cp = System.getProperty("java.class.path");

        String src = "" +
                "package com.wrmsr.tokamak.java;\n" +
                "public class SubThing extends CodegenTest.BaseThing {\n" +
                "    public int x() { return super.x() + 10; }\n" +
                "}\n";

        Class<?> cls = InProcJavaCompiler.compileAndLoad(
                src,
                "com.wrmsr.tokamak.java.SubThing",
                "SubThing",
                ImmutableList.of(
                        "-classpath", cp
                ),
                getClass().getClassLoader());

        BaseThing obj = (BaseThing) cls.getDeclaredConstructor().newInstance();
        System.out.println(obj.x());
    }
}
