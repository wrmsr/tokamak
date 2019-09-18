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
package com.wrmsr.tokamak.main;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.main.util.exec.Execs;
import com.wrmsr.tokamak.main.jna.JnaExecs;

import java.io.File;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkState;

public class ReexecMain
{
    public static File getJvm()
    {
        File jvm = Paths.get(
                System.getProperty("java.home"),
                "bin",
                "java" + (System.getProperty("os.name").startsWith("Win") ? ".exe" : "")
        ).toFile();
        checkState(jvm.exists(), "cannot find jvm: " + jvm.getAbsolutePath());
        checkState(jvm.isFile(), "jvm is not a file: " + jvm.getAbsolutePath());
        checkState(jvm.canExecute(), "jvm is not executable: " + jvm.getAbsolutePath());
        return jvm;
    }

    public static void main(String[] args)
            throws Exception
    {
        Execs execs;

        execs = new JnaExecs();
        // exec = new ProcessBuilderExec();

        // exec.exec("/bin/echo", ImmutableList.of("hi"), ImmutableMap.of());

        String jvm = getJvm().getAbsolutePath();
        String cp = System.getProperty("java.class.path");
        String main = "com.wrmsr.tokamak.main.Main";

        execs.exec(jvm, ImmutableList.of("-cp", cp, main, "-noreexec"));
    }
}
