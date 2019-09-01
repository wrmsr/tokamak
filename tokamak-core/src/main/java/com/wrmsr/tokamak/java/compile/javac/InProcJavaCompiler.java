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
package com.wrmsr.tokamak.java.compile.javac;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.java.compile.javac.option.JavacOption;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.Tool;
import javax.tools.ToolProvider;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class InProcJavaCompiler
{
    /*
    http://www.javased.com/?api=javax.tools.ToolProvider
    https://www.programcreek.com/java-api-examples/?class=javax.tools.ToolProvider&method=getSystemJavaCompiler
    */

    public static void compileJava(List<JavacOption> options, List<File> sourceFiles)
    {
        List<String> args = ImmutableList.<String>builder()
                .addAll(options.stream().map(JavacOption::getArgs).flatMap(List::stream).iterator())
                .addAll(sourceFiles.stream().map(File::getPath).iterator())
                .build();
        Tool javac = ToolProvider.getSystemJavaCompiler();
        javac.run(null, null, null, args.toArray(new String[args.size()]));
    }

    public static Class<?> compileAndLoad(String script, String fullClassName, String simpleClassName, List<String> options)
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        MemoryFileManager memoryFileManager = new MemoryFileManager(standardFileManager);

        JavaFileObject scriptSource = memoryFileManager.createSourceFileObject(null, simpleClassName, script);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                memoryFileManager,
                diagnostics,
                options,
                null,
                Arrays.asList(scriptSource));

        if (!task.call()) {
            String message = diagnostics.getDiagnostics().stream()
                    .map(d -> d.toString())
                    .collect(Collectors.joining("\n"));
            throw new RuntimeException(message);
        }

        ClassLoader classLoader = memoryFileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);
        try {
            return classLoader.loadClass(fullClassName);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
