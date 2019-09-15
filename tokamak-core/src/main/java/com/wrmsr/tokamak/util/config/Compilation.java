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

package com.wrmsr.tokamak.util.config;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.java.lang.JAccess;
import com.wrmsr.tokamak.java.lang.JName;
import com.wrmsr.tokamak.java.lang.JParam;
import com.wrmsr.tokamak.java.lang.JRenderer;
import com.wrmsr.tokamak.java.lang.JTypeSpecifier;
import com.wrmsr.tokamak.java.lang.tree.JInheritance;
import com.wrmsr.tokamak.java.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.java.lang.tree.declaration.JType;
import com.wrmsr.tokamak.java.lang.tree.expression.JLiteral;
import com.wrmsr.tokamak.java.lang.tree.expression.JMethodInvocation;
import com.wrmsr.tokamak.java.lang.tree.statement.JExpressionStatement;
import com.wrmsr.tokamak.java.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.java.lang.unit.JPackageSpec;

import java.util.Optional;

import static com.google.common.collect.Sets.immutableEnumSet;
import static com.wrmsr.tokamak.java.lang.tree.JTrees.jblockify;

public final class Compilation
{
    private Compilation()
    {
    }

    public static void compile(ConfigMetadata md)
    {
        JName ifaceName = new JName(Splitter.on(".").splitToList(md.getCls().getCanonicalName()));
        String bareName = ifaceName.getParts().get(ifaceName.size() - 1);
        JName implName = new JName(
                ImmutableList.<String>builder()
                        .add("com", "wrmsr", "tokamak", "util", "config", "generated")
                        .addAll(ifaceName.getParts())
                        .build());

        JCompilationUnit cu = new JCompilationUnit(
                Optional.of(new JPackageSpec(new JName(implName.getParts().subList(0, implName.size() - 1)))),
                ImmutableSet.of(),
                new JType(
                        immutableEnumSet(JAccess.PUBLIC, JAccess.FINAL),
                        JType.Kind.CLASS,
                        bareName,
                        ImmutableList.of(
                                new JInheritance(
                                        JInheritance.Kind.IMPLEMENTS,
                                        ifaceName)
                        ),
                        ImmutableList.of(
                                new JConstructor(
                                        immutableEnumSet(JAccess.PUBLIC),
                                        bareName,
                                        ImmutableList.of(
                                                new JParam(
                                                        JTypeSpecifier.of("java", "util", "Map"),
                                                        "map")
                                        ),
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
    }
}
