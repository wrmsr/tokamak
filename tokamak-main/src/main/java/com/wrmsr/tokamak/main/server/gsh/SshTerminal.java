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

package com.wrmsr.tokamak.main.server.gsh;

import jline.UnixTerminal;
import org.apache.sshd.server.Environment;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;

public class SshTerminal
        extends UnixTerminal
{
    private static final ThreadLocal<Environment> env = new ThreadLocal<>();

    public SshTerminal()
            throws Exception
    {
        super();
    }

    @Override
    public int getWidth()
    {
        String columnsAsString = retrieveEnvironment().getEnv().get("COLUMNS");
        try {
            if (isNullOrEmpty(columnsAsString)) { return DEFAULT_WIDTH; }

            int columns = parseInt(columnsAsString);
            return columns > 0
                    ? columns
                    : DEFAULT_WIDTH;
        }
        catch (NumberFormatException e) {
            return DEFAULT_WIDTH;
        }
    }

    @Override
    public int getHeight()
    {
        String linesAsString = retrieveEnvironment().getEnv().get("LINES");
        try {
            if (isNullOrEmpty(linesAsString)) {
                return DEFAULT_HEIGHT;
            }

            int lines = parseInt(linesAsString);
            return lines > 0
                    ? lines
                    : DEFAULT_HEIGHT;
        }
        catch (NumberFormatException e) {
            return DEFAULT_HEIGHT;
        }
    }

    private Environment retrieveEnvironment()
    {
        return requireNonNull(env.get(), "Environment is not registered");
    }

    private static boolean isNullOrEmpty(String value)
    {
        return value == null || value.isEmpty();
    }

    static void registerEnvironment(Environment environment)
    {
        env.set(environment);
    }
}
