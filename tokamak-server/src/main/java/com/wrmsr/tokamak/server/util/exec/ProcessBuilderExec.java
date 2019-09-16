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
package com.wrmsr.tokamak.server.util.exec;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProcessBuilderExec
        extends AbstractExec
{
    @Override
    public void exec(String path, List<String> args, Map<String, String> env)
            throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder();

        List<String> command = Lists.newArrayList(path);
        command.addAll(args);
        pb.command(command);
        pb.environment().putAll(env);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = pb.start();
        int ret;
        try {
            ret = process.waitFor();
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        System.exit(ret);
    }
}
