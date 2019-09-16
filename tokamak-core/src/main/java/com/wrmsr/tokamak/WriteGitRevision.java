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
package com.wrmsr.tokamak;

import com.google.common.base.Charsets;
import com.wrmsr.tokamak.util.subprocess.FinalizedProcess;
import com.wrmsr.tokamak.util.subprocess.FinalizedProcessBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class WriteGitRevision
{
    public static String readGitRev(Path git)
            throws IOException, InterruptedException
    {
        String head = new String(Files.readAllBytes(Paths.get(git.toString(), "HEAD")), Charsets.UTF_8).trim();
        String rev;
        if (head.startsWith("ref: ")) {
            String ref = head.substring(5);
            rev = new String(Files.readAllBytes(Paths.get(git.toString(), ref)), Charsets.UTF_8).trim();
        }
        else {
            rev = head;
        }
        return rev + "-injected";
    }

    public static String runGitRev()
            throws IOException, InterruptedException
    {
        FinalizedProcessBuilder pb = new FinalizedProcessBuilder().command(
                "git",
                "describe",
                "--match=NeVeRmAtCh",
                "--always",
                "--abbrev=40",
                "--dirty"
        );
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        List<String> lines = new ArrayList<>();
        try (FinalizedProcess process = pb.start()) {
            process.getOutputStream().close();
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
            process.waitFor(3, TimeUnit.SECONDS);
        }
        if (lines.size() < 1) {
            throw new IllegalStateException();
        }
        String rev = lines.get(0);
        if (!rev.matches("[0-9a-fA-f]{40}(-dirty)?")) {
            throw new IllegalStateException(rev);
        }
        return rev;
    }

    public static void main(String[] args)
            throws Throwable
    {
        boolean write;
        if (args.length == 1) {
            checkArgument(args[0].equals("write"));
            write = true;
        }
        else {
            checkArgument(args.length == 0);
            write = false;
        }

        Path cwd = Paths.get(System.getProperty("user.dir"));
        checkState(cwd.getFileName().toString().equals("tokamak-core"));

        checkState(cwd.getParent().getFileName().toString().equalsIgnoreCase("tokamak"));
        Path git = Paths.get(cwd.getParent().toString(), ".git");
        if (!git.toFile().exists()) {
            return;
        }

        String rev;
        if (Paths.get(git.toString(), "index").toFile().exists()) {
            rev = runGitRev();
        }
        else {
            rev = readGitRev(git);
        }

        if (write) {
            checkState(Paths.get(cwd.toString(), "target").toFile().exists());
            Files.write(
                    Paths.get(cwd.toString(), "target", "classes", "com", "wrmsr", "tokamak", ".revision"),
                    rev.getBytes(Charsets.UTF_8));
        }
        else {
            System.out.println(rev);
        }
    }
}
