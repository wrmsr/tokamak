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
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SubprocessTest
{
    private static final String PYTHON_JSON_SRC = "" +
            "import json\n" +
            "import sys\n" +
            "def _main():\n" +
            "    for line in sys.stdin:\n" +
            "        obj = json.loads(line)\n" +
            "        obj['processed'] = True\n" +
            "        print(json.dumps(obj))\n" +
            "        sys.stdout.flush()\n" +
            "if __name__ == '__main__':\n" +
            "    _main()\n";

    @Test
    public void testSubprocess()
            throws Throwable
    {
        String[] argv = new String[] {
                "/usr/bin/gcc",
                "--version",
        };
        FinalizedProcessBuilder processBuilder = new FinalizedProcessBuilder(argv);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try (FinalizedProcess process = processBuilder.start()) {
            // process.getOutputStream().write(sb.toString().getBytes());
            process.getOutputStream().close();
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }
            process.waitFor(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testPythonSubprocess()
            throws Throwable
    {

        Path tempDir = Files.createTempDirectory("tokamak-temp");
        tempDir.toFile().deleteOnExit();
        Path pyPath = tempDir.resolve("test.py");
        Files.write(pyPath, PYTHON_JSON_SRC.getBytes());

        String[] argv = new String[] {
                "/usr/bin/python",
                pyPath.toFile().getPath(),
        };

        FinalizedProcessBuilder processBuilder = new FinalizedProcessBuilder(argv);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try (FinalizedProcess process = processBuilder.start()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.getOutputStream().write("{\"barf\":true}\n".getBytes());
            process.getOutputStream().flush();
            String line = br.readLine();
            Map obj = Json.readValue(line, Map.class);
            System.out.println(obj);
        }
    }
}
