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
package com.wrmsr.tokamak.main.boot.ops;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.main.boot.Bootstrap;
import com.wrmsr.tokamak.main.jna.JnaExec;
import com.wrmsr.tokamak.main.util.exec.Exec;
import com.wrmsr.tokamak.util.Jdk;

import java.io.File;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class ReexecOp
        implements Bootstrap.Op
{
    private final String mainClsName;
    private final String[] args;

    public ReexecOp(String mainClsName, String[] args)
    {
        this.mainClsName = checkNotEmpty(mainClsName);
        this.args = checkNotNull(args);
    }

    public static final String NO_REEXEC_PROPERTY_KEY = "com.wrmsr.tokamak.main.boot.noreexec";

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

    @Override
    public void run()
            throws Exception
    {
        if (System.getProperties().containsKey(NO_REEXEC_PROPERTY_KEY)) {
            return;
        }

        if (Jdk.isDebug()) {
            return;
        }

        Exec exec;

        exec = new JnaExec();
        // exec = new ProcessBuilderExec();

        String jvm = getJvm().getAbsolutePath();
        String cp = System.getProperty("java.class.path");

        exec.exec(jvm, ImmutableList.<String>builder().add("-cp", cp, "-D" + NO_REEXEC_PROPERTY_KEY, mainClsName).add(args).build());
    }
}
