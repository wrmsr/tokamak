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
package com.wrmsr.tokamak.util;

import com.wrmsr.tokamak.util.subprocess.FinalizedProcess;
import com.wrmsr.tokamak.util.subprocess.FinalizedProcessBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public final class MoreSystem
{
    private MoreSystem()
    {
    }

    public static OptionalLong getPid()
    {
        try {
            Class<?> cls = Class.forName("java.lang.ProcessHandle");
            Object current = cls.getDeclaredMethod("current").invoke(null);
            long pid = (long) cls.getDeclaredMethod("pid").invoke(current);
            return OptionalLong.of(pid);
        }
        catch (ReflectiveOperationException e) {
            return OptionalLong.empty();
        }
    }

    public static String shellEscape(String s)
    {
        return "'" + s.replace("'", "'\"'\"'") + "'";
    }

    public static List<String> runSubprocessLines(List<String> command, long timeoutMillis, boolean inheritStderr)
            throws IOException, InterruptedException
    {
        FinalizedProcessBuilder pb = new FinalizedProcessBuilder().command(command);
        if (inheritStderr) {
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
        List<String> lines = new ArrayList<>();
        try (FinalizedProcess process = pb.start()) {
            process.getOutputStream().close();
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine().trim());
            }
            process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
        }
        return lines;
    }
}
