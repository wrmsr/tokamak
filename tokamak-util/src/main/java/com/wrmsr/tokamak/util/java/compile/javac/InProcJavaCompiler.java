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
package com.wrmsr.tokamak.util.java.compile.javac;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public abstract class InProcJavaCompiler
{
    /*
    http://www.javased.com/?api=javax.tools.ToolProvider
    https://www.programcreek.com/java-api-examples/?class=javax.tools.ToolProvider&method=getSystemJavaCompiler
    https://stackoverflow.com/a/11024944/246071
    */

    protected final String source;
    protected final String fullClassName;
    protected final String simpleClassName;
    protected final List<String> explicitOptions;

    protected final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    protected InProcJavaCompiler(
            String source,
            String fullClassName,
            String simpleClassName,
            Iterable<String> explicitOptions)
    {
        this.source = checkNotEmpty(source);
        this.fullClassName = checkNotEmpty(fullClassName);
        this.simpleClassName = checkNotEmpty(simpleClassName);
        this.explicitOptions = ImmutableList.copyOf(explicitOptions);
    }

    private final SupplierLazyValue<JavaCompiler> compiler = new SupplierLazyValue<>();

    protected final JavaCompiler getCompiler()
    {
        return compiler.get(() -> ToolProvider.getSystemJavaCompiler());
    }

    private final SupplierLazyValue<StandardJavaFileManager> standardFileManager = new SupplierLazyValue<>();

    public StandardJavaFileManager getStandardFileManager()
    {
        return standardFileManager.get(() -> getCompiler().getStandardFileManager(diagnostics, null, null));
    }

    protected JavaFileManager getFileManager()
    {
        return getStandardFileManager();
    }

    protected List<String> getOptions()
    {
        return explicitOptions;
    }

    protected abstract Iterable<JavaFileObject> getSourceFiles();

    protected abstract ClassLoader getClassLoader();

    protected final Class<?> compileAndLoad()
    {
        JavaCompiler.CompilationTask task = getCompiler().getTask(
                null,
                getFileManager(),
                diagnostics,
                getOptions(),
                null,
                getSourceFiles());

        if (!task.call()) {
            String message = diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new RuntimeException(message);
        }

        try {
            return Class.forName(fullClassName, true, getClassLoader());
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MemoryImpl
            extends InProcJavaCompiler
    {
        private final ClassLoader parentClassLoader;

        public MemoryImpl(
                String source,
                String fullClassName,
                String simpleClassName,
                Iterable<String> explicitOptions,
                ClassLoader parentClassLoader)
        {
            super(source, fullClassName, simpleClassName, explicitOptions);

            this.parentClassLoader = checkNotNull(parentClassLoader);
        }

        private final SupplierLazyValue<MemoryFileManager> fileManager = new SupplierLazyValue<>();

        @Override
        protected MemoryFileManager getFileManager()
        {
            return fileManager.get(() -> new MemoryFileManager(getStandardFileManager()));
        }

        private final SupplierLazyValue<JavaFileObject> sourceFiles = new SupplierLazyValue<>();

        protected JavaFileObject getSourceFile()
        {
            return sourceFiles.get(() -> getFileManager().createSourceFileObject(null, simpleClassName, source));
        }

        @Override
        protected Iterable<JavaFileObject> getSourceFiles()
        {
            return ImmutableList.of(getSourceFile());
        }

        private final SupplierLazyValue<ClassLoader> classloader = new SupplierLazyValue<>();

        @Override
        protected ClassLoader getClassLoader()
        {
            return classloader.get(() -> getFileManager().createMemoryClassLoader(parentClassLoader));
        }
    }

    public static Class<?> compileAndLoad(
            String script,
            String fullClassName,
            String simpleClassName,
            List<String> options,
            ClassLoader parentClassLoader)
    {
        return new MemoryImpl(script, fullClassName, simpleClassName, options, parentClassLoader).compileAndLoad();
    }

    private static abstract class AbstractTempFile
            extends InProcJavaCompiler
    {
        public AbstractTempFile(
                String source,
                String fullClassName,
                String simpleClassName,
                Iterable<String> explicitOptions)
        {
            super(source, fullClassName, simpleClassName, explicitOptions);
        }

        private final SupplierLazyValue<Path> tempDir = new SupplierLazyValue<>();

        public Path getTempDir()
        {
            return tempDir.get(() -> {
                try {
                    Path tempDir = createTempDirectory();
                    tempDir.toFile().deleteOnExit();
                    return tempDir;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private final SupplierLazyValue<Path> sourceFile = new SupplierLazyValue<>();

        public Path getSourceFile()
        {
            return sourceFile.get(() -> {
                try {
                    List<String> parts = Splitter.on('.').splitToList(fullClassName);
                    Path pkgDir = Files.createDirectories(
                            Paths.get(getTempDir().toString() + "/" + Joiner.on('/').join(parts.subList(0, parts.size() - 1))));
                    Path sourceFile = pkgDir.resolve(simpleClassName + ".java");
                    Files.write(sourceFile, source.getBytes(Charsets.UTF_8));
                    return sourceFile;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        protected Iterable<JavaFileObject> getSourceFiles()
        {
            return ImmutableList.copyOf(getStandardFileManager().getJavaFileObjects(getSourceFile().toFile()));
        }

        protected URL getClassLoaderUrl()
        {
            try {
                return new URL("file://" + getTempDir().toAbsolutePath().toString() + "/");
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected abstract ClassLoader newClassLoader();

        private final SupplierLazyValue<ClassLoader> classloader = new SupplierLazyValue<>();

        @Override
        protected ClassLoader getClassLoader()
        {
            return classloader.get(this::newClassLoader);
        }
    }

    public static Class<?> compileAndLoadFromTempFile(
            String script,
            String fullClassName,
            String simpleClassName,
            List<String> options,
            ClassLoader parentClassLoader)
    {
        return new AbstractTempFile(script, fullClassName, simpleClassName, options)
        {
            @Override
            protected ClassLoader newClassLoader()
            {
                return new URLClassLoader(new URL[] {getClassLoaderUrl()}, parentClassLoader);
            }
        }.compileAndLoad();
    }

    public static Class<?> compileAndLoadFromTempFileAndInjet(
            String script,
            String fullClassName,
            String simpleClassName,
            List<String> options,
            URLClassLoader urlClassLoader)
    {
        return new AbstractTempFile(script, fullClassName, simpleClassName, options)
        {
            @Override
            protected ClassLoader newClassLoader()
            {
                try {
                    URLClassLoader.class.getDeclaredMethod("addURL", URL.class).invoke(urlClassLoader, getClassLoaderUrl());
                    return urlClassLoader;
                }
                catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }.compileAndLoad();
    }
}
