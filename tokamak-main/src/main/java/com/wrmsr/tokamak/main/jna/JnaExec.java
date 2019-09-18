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
package com.wrmsr.tokamak.main.jna;

import com.google.common.collect.ImmutableList;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.wrmsr.tokamak.main.util.exec.AbstractExec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JnaExec
        extends AbstractExec
{
    public interface Libc
            extends Library
    {
        void execve(String pathname, String argv[], String envp[]);
    }

    @Override
    public void exec(String path, List<String> args, Map<String, String> env)
            throws IOException
    {
        String[] convertedArgs = ImmutableList.<String>builder().add(path).addAll(args).build().toArray(new String[] {});
        String[] convertedEnv = convertEnv(env);

        Libc libc = Native.load((Platform.isWindows() ? "msvcrt" : "c"), Libc.class);

        libc.execve(path, convertedArgs, convertedEnv);
    }
}
