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
import com.wrmsr.tokamak.java.lang.JAccess;
import com.wrmsr.tokamak.java.lang.JName;
import com.wrmsr.tokamak.java.lang.JQualifiedName;
import com.wrmsr.tokamak.java.lang.JRenderer;
import com.wrmsr.tokamak.java.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.java.lang.tree.declaration.JDeclaration;
import com.wrmsr.tokamak.java.lang.tree.declaration.JType;
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
    public void testCodegen()
    {
        JCompilationUnit cu = new JCompilationUnit(
                Optional.of(new JPackageSpec(JQualifiedName.of("com", "wrmsr", "tokamak"))),
                ImmutableSet.of(),
                new JType(
                        immutableEnumSet(JAccess.PUBLIC, JAccess.FINAL),
                        JType.Kind.CLASS,
                        "Thing",
                        ImmutableList.of(),
                        ImmutableList.<JDeclaration>of(
                                new JConstructor(
                                        immutableEnumSet(JAccess.PUBLIC),
                                        "Thing",
                                        ImmutableList.of(),
                                        jblockify(
                                                new JExpressionStatement(
                                                        JMethodInvocation.of(
                                                                JQualifiedName.of("super"),
                                                                ImmutableList.of())))))));

        String rendered = JRenderer.renderWithIndent(cu, "    ");
        System.out.println(rendered);
    }
}
