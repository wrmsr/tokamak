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
import com.wrmsr.tokamak.util.config.props.BaseConfigPropertyImpl;
import com.wrmsr.tokamak.util.config.props.BooleanConfigProperty;
import com.wrmsr.tokamak.util.config.props.BooleanConfigPropertyImpl;
import com.wrmsr.tokamak.util.config.props.ConfigPropertyImpl;
import com.wrmsr.tokamak.util.config.props.IntConfigProperty;
import com.wrmsr.tokamak.util.config.props.IntConfigPropertyImpl;
import com.wrmsr.tokamak.util.func.BooleanConsumer;
import com.wrmsr.tokamak.util.java.compile.javac.InProcJavaCompiler;
import com.wrmsr.tokamak.util.java.lang.JAccess;
import com.wrmsr.tokamak.util.java.lang.JName;
import com.wrmsr.tokamak.util.java.lang.JParam;
import com.wrmsr.tokamak.util.java.lang.JRenderer;
import com.wrmsr.tokamak.util.java.lang.JTypeSpecifier;
import com.wrmsr.tokamak.util.java.lang.tree.JInheritance;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JConstructor;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JDeclaration;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JField;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JMethod;
import com.wrmsr.tokamak.util.java.lang.tree.declaration.JType;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JAssignment;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JCast;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JIdent;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JLambda;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JLiteral;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JMemberAccess;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JMethodInvocation;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JMethodReference;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JNew;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JExpressionStatement;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JReturn;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JStatement;
import com.wrmsr.tokamak.util.java.lang.unit.JCompilationUnit;
import com.wrmsr.tokamak.util.java.lang.unit.JPackageSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.immutableEnumSet;
import static com.wrmsr.tokamak.util.java.lang.tree.JTrees.jblockify;

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
        private final boolean instantiatedMetadata;

        public CompiledConfig(
                ConfigMetadata metadata,
                String fullClassName,
                String bareName,
                JCompilationUnit compilationUnit,
                boolean instantiatedMetadata)
        {
            this.metadata = checkNotNull(metadata);
            this.fullClassName = checkNotNull(fullClassName);
            this.bareName = checkNotNull(bareName);
            this.compilationUnit = checkNotNull(compilationUnit);
            this.instantiatedMetadata = instantiatedMetadata;
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

        public boolean isInstantiatedMetadata()
        {
            return instantiatedMetadata;
        }
    }

    public interface Construction
    {
        BooleanConfigProperty buildBooleanPropertyImpl(String name, BooleanSupplier getter, BooleanConsumer setter);

        <T> ConfigPropertyImpl<T> buildPropertyImpl(String name, Supplier<T> getter, Consumer<T> setter);

        IntConfigProperty buildIntPropertyImpl(String name, IntSupplier getter, IntConsumer setter);
    }

    public static final class ConstructionImpl
            implements Construction
    {
        private final ConfigMetadata metadata;

        public ConstructionImpl(ConfigMetadata metadata)
        {
            this.metadata = checkNotNull(metadata);
        }

        public static String getPropertyImplBuilderMethodName(ConfigPropertyMetadata pmd)
        {
            Class<? extends BaseConfigPropertyImpl> propImplCls = pmd.getImplCls();
            if (propImplCls == BooleanConfigPropertyImpl.class) {
                return "buildBooleanPropertyImpl";
            }
            else if (propImplCls == ConfigPropertyImpl.class) {
                return "buildPropertyImpl";
            }
            else if (propImplCls == IntConfigPropertyImpl.class) {
                return "buildIntPropertyImpl";
            }
            else {
                throw new IllegalArgumentException(propImplCls.toString());
            }
        }

        @Override
        public BooleanConfigProperty buildBooleanPropertyImpl(String name, BooleanSupplier getter, BooleanConsumer setter)
        {
            return new BooleanConfigPropertyImpl(
                    metadata.getProperty(name),
                    getter,
                    setter);
        }

        @Override
        public <T> ConfigPropertyImpl<T> buildPropertyImpl(String name, Supplier<T> getter, Consumer<T> setter)
        {
            return new ConfigPropertyImpl<>(
                    metadata.getProperty(name),
                    getter,
                    setter);
        }

        @Override
        public IntConfigProperty buildIntPropertyImpl(String name, IntSupplier getter, IntConsumer setter)
        {
            return new IntConfigPropertyImpl(
                    metadata.getProperty(name),
                    getter,
                    setter);
        }
    }

    @FunctionalInterface
    public interface ImplConstructionFactory<T extends Config>
    {
        T build(Construction construction);
    }

    public static CompiledConfig compile(ConfigMetadata md, boolean instantiateMetadata)
    {
        JName ifaceName = new JName(Splitter.on(".").splitToList(md.getCls().getCanonicalName()));
        JName mangledIfaceName = new JName(
                Splitter.on(".").splitToList(md.getCls().getName()).stream()
                        .map(p -> p.replaceAll("\\$", "__"))
                        .collect(Collectors.toList()));

        String bareName = mangledIfaceName.getParts().get(mangledIfaceName.size() - 1);
        JName implName = new JName(
                ImmutableList.<String>builder()
                        .add("com", "wrmsr", "tokamak", "util", "config", "generated")
                        .addAll(mangledIfaceName.getParts())
                        .build());

        List<JDeclaration> fields = new ArrayList<>();
        List<JStatement> ctor = new ArrayList<>();
        List<JDeclaration> getters = new ArrayList<>();

        md.getProperties().values().forEach(prop -> {
            JTypeSpecifier ts = JTypeSpecifier.of(prop.getType());
            Class<? extends BaseConfigPropertyImpl> ic = prop.getImplCls();
            JTypeSpecifier pts = new JTypeSpecifier(
                    JName.of(ic),
                    ic.getTypeParameters().length > 0 ? Optional.of(ImmutableList.of(ts)) : Optional.empty(),
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
                                                            JIdent.of("construction"),
                                                            ConstructionImpl.getPropertyImplBuilderMethodName(prop)),
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
                                JName.of(ImplConstructionFactory.class),
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

        if (instantiateMetadata) {
            fields.add(
                    new JField(
                            immutableEnumSet(JAccess.PUBLIC, JAccess.STATIC, JAccess.FINAL),
                            JTypeSpecifier.of(ConfigMetadata.class),
                            "METADATA",
                            Optional.of(
                                    new JNew(
                                            JTypeSpecifier.of(ConfigMetadata.class),
                                            ImmutableList.of(
                                                    new JMemberAccess(new JIdent(JName.of(md.getCls().getCanonicalName())), "class")
                                            )))));
        }
        else {
            fields.add(
                    new JField(
                            immutableEnumSet(JAccess.PUBLIC, JAccess.STATIC, JAccess.VOLATILE),
                            JTypeSpecifier.of(ConfigMetadata.class),
                            "METADATA",
                            Optional.empty()));
        }

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
                                                                JTypeSpecifier.of(JName.of(Construction.class)),
                                                                "construction")
                                                ),
                                                jblockify(ctor)))
                                .addAll(getters)
                                .build()
                ));

        return new
                CompiledConfig(
                md,
                implName.join(),
                bareName,
                cu,
                instantiateMetadata);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Class<? extends T> compileAndLoadWithOptions(
            ConfigMetadata metadata,
            List<String> options,
            ClassLoader classLoader)
    {
        CompiledConfig compiled = compile(metadata, false);
        String src = JRenderer.renderWithIndent(compiled.getCompilationUnit(), "    ");
        Class<? extends T> impl = (Class<? extends T>) InProcJavaCompiler.compileAndLoad(
                src,
                compiled.getFullClassName(),
                compiled.getBareName(),
                options,
                classLoader);
        try {
            impl.getDeclaredField("METADATA").set(null, metadata);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return impl;
    }

    public static <T> Class<? extends T> compileAndLoad(
            ConfigMetadata metadata,
            String classPath,
            ClassLoader classLoader)
    {
        return compileAndLoadWithOptions(
                metadata,
                ImmutableList.of(
                        "-source", "1.8",
                        "-target", "1.8",
                        "-classpath", classPath
                ),
                classLoader);
    }

    public static <T> Class<? extends T> compileAndLoad(ConfigMetadata metadata)
    {
        return compileAndLoad(
                metadata,
                System.getProperty("java.class.path"),
                Compilation.class.getClassLoader());
    }

    @FunctionalInterface
    public interface ImplFactory<T extends Config>
    {
        T build(ConfigMetadata metadata);
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends Config> ImplFactory<T> getImplFactory(Class<? extends T> impl)
    {
        ImplConstructionFactory<T> implConstructionFactory;
        try {
            implConstructionFactory = (ImplConstructionFactory<T>) impl.getDeclaredField("FACTORY").get(null);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return metadata -> implConstructionFactory.build(new ConstructionImpl(metadata));
    }
}
