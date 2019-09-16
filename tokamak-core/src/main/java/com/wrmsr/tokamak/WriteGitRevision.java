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
    /*
    git --version >/dev/null 2>/dev/null
    RET=$?
    if [ $RET -ne 0 ]; then
        exit 0
    fi
    set -e
    if [ "$""{PWD##*\/}" != "tokamak" ] ; then
        cd ..
    fi
    if [ -f ".revision" ] ; then
        rm .revision
    fi
    if [ -d ".git" ] ; then
        REV=$(cat .git/HEAD)
        REV_REF=$(echo "$REV" | egrep '^ref: ' | cut -c 6-)
        if [ -f ".git/$REV_REF" ] ; then
            cat ".git/$REV_REF" > .revision
        else
            echo "$REV" > .revision
        fi
    fi

    REV=$(cat .git/HEAD) && \
    REV_REF=$(echo "$REV" | egrep '^ref: ' | cut -c 6-) && \
    (if [ -f ".git/$REV_REF" ] ; then \
        cat ".git/$REV_REF" > .revision ; \
    else \
        echo "$REV" > .revision ; \
    fi) \
    */

    public static String readGitRev(Path git)
            throws IOException, InterruptedException
    {
        String head = new String(Files.readAllBytes(Paths.get(git.toString(), "HEAD")), Charsets.UTF_8).trim();
        if (head.startsWith("ref: ")) {
            String ref = head.substring(5);
            return new String(Files.readAllBytes(Paths.get(git.toString(), ref)), Charsets.UTF_8).trim();
        }
        else {
            return head;
        }
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
        checkArgument(args.length == 0);
        Path path = Paths.get(System.getProperty("user.dir"));
        checkState(path.getFileName().toString().equals("tokamak-core"));

        checkState(path.getParent().getFileName().toString().equalsIgnoreCase("tokamak"));
        Path git = Paths.get(path.getParent().toString(), ".git");
        if (!git.toFile().exists()) {
            return;
        }

        String rev;
        if (false) { //Paths.get(git.toString(), "index").toFile().exists()) {
            rev = runGitRev();
        }
        else {
            rev = readGitRev(git);
        }
        System.out.println(rev);
    }
}
