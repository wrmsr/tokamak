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
package com.wrmsr.tokamak.main.repl;

import com.google.common.base.Strings;
import com.wrmsr.tokamak.main.boot.Bootstrap;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.MemoryHistory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Files.createParentDirs;
import static java.lang.Integer.parseInt;
import static java.util.Locale.ENGLISH;
import static jline.internal.Configuration.getUserHome;

public class ReplMain
{
    private static MemoryHistory getHistory()
    {
        String historyFilePath = System.getenv("TOKAMAK_HISTORY_FILE");
        File historyFile;
        if (isNullOrEmpty(historyFilePath)) {
            historyFile = new File(getUserHome(), ".tokamak_history");
        }
        else {
            historyFile = new File(historyFilePath);
        }
        return getHistory(historyFile);
    }

    private static MemoryHistory getHistory(File historyFile)
    {
        MemoryHistory history;
        try {
            //  try creating the history file and its parents to check
            // whether the directory tree is readable/writeable
            createParentDirs(historyFile.getParentFile());
            historyFile.createNewFile();
            history = new FileHistory(historyFile);
            history.setMaxSize(10000);
        }
        catch (IOException e) {
            System.err.printf("WARNING: Failed to load history file (%s): %s. " +
                            "History will not be available during this session.%n",
                    historyFile, e.getMessage());
            history = new MemoryHistory();
        }
        history.setAutoTrim(true);
        return history;
    }

    private static final Pattern HISTORY_INDEX_PATTERN = Pattern.compile("!\\d+");

    private static void runConsole(AtomicBoolean exiting)
    {
        try (LineReader reader = new LineReader(getHistory())) {
            StringBuilder buffer = new StringBuilder();
            while (!exiting.get()) {
                // read a line of input from user
                String prompt = "tokamak";
                if (buffer.length() > 0) {
                    prompt = Strings.repeat(" ", prompt.length() - 1) + "-";
                }
                String commandPrompt = prompt + "> ";
                String line = reader.readLine(commandPrompt);

                // add buffer to history and clear on user interrupt
                if (reader.interrupted()) {
                    String partial = ""; // squeezeStatement(buffer.toString());
                    if (!partial.isEmpty()) {
                        reader.getHistory().add(partial);
                    }
                    buffer = new StringBuilder();
                    continue;
                }

                // exit on EOF
                if (line == null) {
                    System.out.println();
                    return;
                }

                // check for special commands if this is the first line
                if (buffer.length() == 0) {
                    String command = line.trim();

                    if (HISTORY_INDEX_PATTERN.matcher(command).matches()) {
                        int historyIndex = parseInt(command.substring(1));
                        History history = reader.getHistory();
                        if ((historyIndex <= 0) || (historyIndex > history.index())) {
                            System.err.println("Command does not exist");
                            continue;
                        }
                        line = history.get(historyIndex - 1).toString();
                        System.out.println(commandPrompt + line);
                    }

                    if (command.endsWith(";")) {
                        command = command.substring(0, command.length() - 1).trim();
                    }

                    switch (command.toLowerCase(ENGLISH)) {
                        case "exit":
                        case "quit":
                            return;
                        case "history":
                            for (History.Entry entry : reader.getHistory()) {
                                System.out.printf("%5d  %s%n", entry.index() + 1, entry.value());
                            }
                            continue;
                        case "help":
                            System.out.println();
                            System.out.println("help");
                            continue;
                    }
                }

                // not a command, add line to buffer
                buffer.append(line).append("\n");

                // execute any complete statements
                String sql = buffer.toString();
                System.out.println(sql);
            }
        }
        catch (IOException e) {
            System.err.println("Readline error: " + e.getMessage());
        }
    }

    public static void main(String[] args)
            throws Throwable
    {
        Bootstrap.bootstrap();
        runConsole(new AtomicBoolean());
    }
}
