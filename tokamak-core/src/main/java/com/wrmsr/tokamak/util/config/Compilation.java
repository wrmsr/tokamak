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
import com.wrmsr.tokamak.java.compile.javac.InProcJavaCompiler;
import com.wrmsr.tokamak.java.lang.JAccess;
import com.wrmsr.tokamak.java.lang.JName;
import com.wrmsr.tokamak.java.lang.JParam;
import com.wrmsr.tokamak.java.lang.JRenderer;
import com.wrmsr.tokamak.java.lang.JTypeSpecifier;
import com.wrmsr.tokamak.java.lang.tree.JInheritance;
import com.wrmsr.tokamak.java.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.java.lang.tree.declaration.JDeclaration;
import com.wrmsr.tokamak.java.lang.tree.declaration.JField;
import com.wrmsr.tokamak.java.lang.tree.declaration.JMethod;
import com.wrmsr.tokamak.java.lang.tree.declaration.JType;
import com.wrmsr.tokamak.java.lang.tree.expression.JAssignment;
import com.wrmsr.tokamak.java.lang.tree.expression.JCast;
import com.wrmsr.tokamak.java.lang.tree.expression.JIdent;
import com.wrmsr.tokamak.java.lang.tree.expression.JLambda;
import com.wrmsr.tokamak.java.lang.tree.expression.JLiteral;
import com.wrmsr.tokamak.java.lang.tree.expression.JMemberAccess;
import com.wrmsr.tokamak.java.lang.tree.expression.JMethodInvocation;
import com.wrmsr.tokamak.java.lang.tree.expression.JMethodReference;
import com.wrmsr.tokamak.java.lang.tree.statement.JExpressionStatement;
import com.wrmsr.tokamak.java.lang.tree.statement.JReturn;
import com.wrmsr.tokamak.java.lang.tree.statement.JStatement;
import com.wrmsr.tokamak.java.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.java.lang.unit.JPackageSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.immutableEnumSet;
import static com.wrmsr.tokamak.java.lang.tree.JTrees.jblockify;

public final class Compilation
{
    private Compilation()
    {
    }

    public static final class CompiledConfig
    {
        private final ConfigMetadata metadata;
        private final String fullClassName;
        private final String bareName;
        private final JCompilationUnit compilationUnit;

        public CompiledConfig(ConfigMetadata metadata, String fullClassName, String bareName, JCompilationUnit compilationUnit)
        {
            this.metadata = checkNotNull(metadata);
            this.fullClassName = checkNotNull(fullClassName);
            this.bareName = checkNotNull(bareName);
            this.compilationUnit = checkNotNull(compilationUnit);
        }

        public ConfigMetadata getMetadata()
        {
            return metadata;
        }

        public String getFullClassName()
        {
            return fullClassName;
        }

        public String getBareName()
        {
            return bareName;
        }

        public JCompilationUnit getCompilationUnit()
        {
            return compilationUnit;
        }
    }

    public static CompiledConfig compile(ConfigMetadata md)
    {
        JName ifaceName = new JName(Splitter.on(".").splitToList(md.getCls().getCanonicalName()));
        String bareName = ifaceName.getParts().get(ifaceName.size() - 1);
        JName implName = new JName(
                ImmutableList.<String>builder()
                        .add("com", "wrmsr", "tokamak", "util", "config", "generated")
                        .addAll(ifaceName.getParts())
                        .build());

        List<JDeclaration> fields = new ArrayList<>();
        List<JStatement> ctor = new ArrayList<>();
        List<JDeclaration> getters = new ArrayList<>();

        md.getProperties().values().forEach(prop -> {
            JTypeSpecifier ts = JTypeSpecifier.of(prop.getType());
            JTypeSpecifier pts = new JTypeSpecifier(
                    JName.of(prop.getImplCls()),
                    Optional.of(ImmutableList.of(ts)),
                    ImmutableList.of());

            fields.add(
                    new JField(
                            immutableEnumSet(JAccess.PRIVATE, JAccess.VOLATILE),
                            ts,
                            "_" + prop.getName(),
                            Optional.empty()));

            fields.add(
                    new JField(
                            immutableEnumSet(JAccess.PRIVATE, JAccess.FINAL),
                            pts,
                            prop.getName(),
                            Optional.empty()));

            ctor.add(
                    new JExpressionStatement(
                            new JAssignment(
                                    JIdent.of(prop.getName()),
                                    new JCast(
                                            pts,
                                            new JMethodInvocation(
                                                    new JMemberAccess(
                                                            JIdent.of("metadata"),
                                                            md.getPropertyImplBuilderMethodName(prop)),
                                                    ImmutableList.of(
                                                            new JLiteral(prop.getName()),
                                                            new JLambda(
                                                                    ImmutableList.of(),
                                                                    jblockify(
                                                                            new JReturn(
                                                                                    Optional.of(
                                                                                            JIdent.of("_" + prop.getName()))))),
                                                            new JLambda(
                                                                    ImmutableList.of(
                                                                            "_" + prop.getName()
                                                                    ),
                                                                    jblockify(
                                                                            new JExpressionStatement(
                                                                                    new JAssignment(
                                                                                            new JMemberAccess(
                                                                                                    JIdent.of("this"),
                                                                                                    "_" + prop.getName()),
                                                                                            JIdent.of("_" + prop.getName())))))
                                                    ))))));

            getters.add(
                    new JMethod(
                            immutableEnumSet(JAccess.PUBLIC),
                            pts,
                            prop.getName(),
                            ImmutableList.of(),
                            Optional.of(
                                    jblockify(
                                            new JReturn(
                                                    Optional.of(
                                                            new JIdent(JName.of(prop.getName()))))
                                    ))));
        });

        fields.add(
                new JField(
                        immutableEnumSet(JAccess.PUBLIC, JAccess.STATIC, JAccess.FINAL),
                        new JTypeSpecifier(
                                JName.of(ImplFactory.class),
                                Optional.of(
                                        ImmutableList.of(
                                                JTypeSpecifier.of(bareName)
                                        )),
                                ImmutableList.of()),
                        "FACTORY",
                        Optional.of(
                                new JMethodReference(
                                        new JIdent(JName.of(bareName)),
                                        "new"))));

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
                        ImmutableList.<JDeclaration>builder()
                                .addAll(fields)
                                .add(
                                        new JConstructor(
                                                immutableEnumSet(JAccess.PUBLIC),
                                                bareName,
                                                ImmutableList.of(
                                                        new JParam(
                                                                JTypeSpecifier.of(JName.of(ConfigMetadata.class)),
                                                                "metadata")
                                                ),
                                                jblockify(ctor)))
                                .addAll(getters)
                                .build()
                ));

        return new CompiledConfig(
                md,
                implName.join(),
                bareName,
                cu);
    }

    public static Class<?> compileAndLoad(ConfigMetadata md)
    {
        CompiledConfig compiled = compile(md);
        String src = JRenderer.renderWithIndent(compiled.getCompilationUnit(), "    ");
        String cp = System.getProperty("java.class.path");
        Class<?> impl = InProcJavaCompiler.compileAndLoad(
                src,
                compiled.getFullClassName(),
                compiled.getBareName(),
                ImmutableList.of(
                        "-classpath", cp
                ),
                Compilation.class.getClassLoader());
        return impl;
    }

    @FunctionalInterface
    public interface ImplFactory<T extends Config>
    {
        T build(ConfigMetadata metadata);
    }
}
