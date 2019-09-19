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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.java.lang.JRenderer;

import javax.tools.Tool;
import javax.tools.ToolProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;

public class CompileAll
{
    public static void main(String[] args)
            throws Throwable
    {
        Path cwd = Paths.get(System.getProperty("user.dir"));

        List<String> compileOpts = ImmutableList.of(
                "-source", "1.8",
                "-target", "1.8",
                "-classpath", System.getProperty("java.class.path")
        );

        Path tempPath = createTempDirectory("tokamak-config-compile-all");

        Path classes = Paths.get(cwd.toString(), "target", "classes");
        Files.walk(classes)
                .filter(Files::isRegularFile)
                .forEach(f -> {
                    if (!f.getFileName().toString().endsWith(".class")) {
                        return;
                    }

                    String relpath = classes.relativize(f).toString();
                    String className = relpath.substring(0, relpath.length() - 6).replaceAll("/", ".");

                    Class<? extends Config> cls;
                    try {
                        cls = (Class<? extends Config>) Class.forName(className);
                    }
                    catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    if (!Config.class.isAssignableFrom(cls) || cls == Config.class) {
                        return;
                    }

                    Compilation.CompiledConfig compiled = Compilation.compile(new ConfigMetadata(cls), true);
                    String src = JRenderer.renderWithIndent(compiled.getCompilationUnit(), "    ");

                    Path implJavaPath = Paths.get(tempPath.toString(), compiled.getBareName() + ".java");
                    Path implClassPath = Paths.get(tempPath.toString(), compiled.getBareName() + ".class");
                    Path targetImplClassPath = Paths.get(classes.toString(), compiled.getFullClassName().replaceAll("\\.", "/") + ".class");
                    try {
                        Files.write(implJavaPath, src.getBytes(Charsets.UTF_8));

                        List<String> jcargs = ImmutableList.<String>builder()
                                .addAll(compileOpts)
                                .add(implJavaPath.toString())
                                .build();
                        Tool javac = ToolProvider.getSystemJavaCompiler();
                        int ret = javac.run(null, System.out, System.err, jcargs.toArray(new String[jcargs.size()]));
                        if (ret != 0) {
                            throw new RuntimeException("Compilation failed");
                        }

                        targetImplClassPath.toFile().getParentFile().mkdirs();
                        Files.copy(implClassPath, targetImplClassPath);

                        Class implCls = Class.forName(compiled.getFullClassName());
                        checkState(cls.isAssignableFrom(implCls));
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
