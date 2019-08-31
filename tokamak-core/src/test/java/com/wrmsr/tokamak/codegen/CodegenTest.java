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
package com.wrmsr.tokamak.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.codegen.lang.JAccess;
import com.wrmsr.tokamak.codegen.lang.JName;
import com.wrmsr.tokamak.codegen.lang.JQualifiedName;
import com.wrmsr.tokamak.codegen.lang.JRenderer;
import com.wrmsr.tokamak.codegen.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.codegen.lang.tree.declaration.JDeclaration;
import com.wrmsr.tokamak.codegen.lang.tree.declaration.JType;
import com.wrmsr.tokamak.codegen.lang.tree.expression.JMethodInvocation;
import com.wrmsr.tokamak.codegen.lang.tree.statement.JExpressionStatement;
import com.wrmsr.tokamak.codegen.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.codegen.lang.unit.JPackageSpec;
import junit.framework.TestCase;

import java.util.Optional;

import static com.google.common.collect.Sets.immutableEnumSet;
import static com.wrmsr.tokamak.codegen.lang.tree.JTrees.jblockify;

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
                        JName.of("Thing"),
                        ImmutableList.of(),
                        ImmutableList.<JDeclaration>of(
                                new JConstructor(
                                        immutableEnumSet(JAccess.PUBLIC),
                                        JName.of("Thing"),
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
