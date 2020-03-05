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

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.Tool;
import javax.tools.ToolProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class InProcJavaCompiler
{
    /*
    http://www.javased.com/?api=javax.tools.ToolProvider
    https://www.programcreek.com/java-api-examples/?class=javax.tools.ToolProvider&method=getSystemJavaCompiler
    */

    // public static void compile(List<JavacOption> options, List<File> sourceFiles)
    // {
    //     List<String> args = ImmutableList.<String>builder()
    //             .addAll(options.stream().map(JavacOption::getArgs).flatMap(List::stream).iterator())
    //             .addAll(sourceFiles.stream().map(File::getPath).iterator())
    //             .build();
    //     Tool javac = ToolProvider.getSystemJavaCompiler();
    //     javac.run(null, null, null, args.toArray(new String[args.size()]));
    // }

    public static void compile(List<String> options, List<File> sourceFiles)
    {
        List<String> args = ImmutableList.<String>builder()
                .addAll(options)
                .addAll(sourceFiles.stream().map(File::getPath).iterator())
                .build();
        Tool javac = ToolProvider.getSystemJavaCompiler();
        javac.run(null, null, null, args.toArray(new String[args.size()]));
    }

    private static void compileInner(
            JavaCompiler compiler,
            JavaFileManager fileManager,
            DiagnosticCollector<JavaFileObject> diagnostics,
            List<String> options,
            Iterable<JavaFileObject> scriptSources)
    {
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                scriptSources);

        if (!task.call()) {
            String message = diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new RuntimeException(message);
        }
    }

    public static Class<?> compileAndLoad(
            String script,
            String fullClassName,
            String simpleClassName,
            List<String> options,
            ClassLoader parentClassLoader)
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);

        MemoryFileManager memoryFileManager = new MemoryFileManager(standardFileManager);
        JavaFileObject scriptSource = memoryFileManager.createSourceFileObject(null, simpleClassName, script);

        compileInner(
                compiler,
                memoryFileManager,
                diagnostics,
                options,
                ImmutableList.of(scriptSource));

        ClassLoader classLoader = memoryFileManager.createMemoryClassLoader(parentClassLoader);
        try {
            return classLoader.loadClass(fullClassName);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> compileAndLoadFromTempFile(
            String script,
            String fullClassName,
            String simpleClassName,
            List<String> options,
            ClassLoader parentClassLoader)
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Path tempDir;
        Path sourceFilePath;
        try {
            tempDir = createTempDirectory();
            tempDir.toFile().deleteOnExit();
            List<String> parts = Splitter.on('.').splitToList(fullClassName);
            Path pkgDir = Files.createDirectories(
                    Paths.get(tempDir.toString() + "/" + Joiner.on('/').join(parts.subList(0, parts.size() - 1))));
            sourceFilePath = pkgDir.resolve(simpleClassName + ".java");
            Files.write(sourceFilePath, script.getBytes(Charsets.UTF_8));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaFileObject scriptSource = checkSingle(standardFileManager.getJavaFileObjects(sourceFilePath.toFile()));

        compileInner(
                compiler,
                standardFileManager,
                diagnostics,
                options,
                ImmutableList.of(scriptSource));

        ClassLoader classLoader;
        try {
            URL url = new URL("file://" + tempDir.toAbsolutePath().toString() + "/");
            classLoader = new URLClassLoader(new URL[] {url}, parentClassLoader);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return classLoader.loadClass(fullClassName);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
