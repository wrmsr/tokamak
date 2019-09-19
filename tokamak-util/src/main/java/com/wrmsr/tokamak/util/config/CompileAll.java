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

import com.wrmsr.tokamak.util.java.lang.JRenderer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompileAll
{
    public static void main(String[] args)
            throws Throwable
    {
        Path cwd = Paths.get(System.getProperty("user.dir"));

        Path classes = Paths.get(cwd.toString(), "target", "classes");
        Files.walk(classes)
                .filter(Files::isRegularFile)
                .forEach(f -> {
                    if (!f.getFileName().toString().endsWith(".class")) {
                        return;
                    }
                    String className = classes.relativize(f).toString().substring(
                            0, classes.relativize(f).toString().length() - 6).replaceAll("/", ".");
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
                    System.out.println(src);
                });
    }
}
