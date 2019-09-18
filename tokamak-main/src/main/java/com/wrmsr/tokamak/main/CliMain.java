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

import com.wrmsr.tokamak.main.bootstrap.Bootstrap;
import picocli.CommandLine;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.concurrent.Callable;

public class CliMain
{
    @CommandLine.Command(name = "checksum", mixinStandardHelpOptions = true, version = "checksum 4.0",
            description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
    public static class CheckSum
            implements Callable<Integer>
    {
        @CommandLine.Parameters(index = "0", description = "The file whose checksum to calculate.")
        private File file;

        @CommandLine.Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
        private String algorithm = "MD5";

        // this example implements Callable, so parsing, error handling and handling user
        // requests for usage help or version help can be done with one line of code.
        public static void main(String... args)
        {
            int exitCode = new CommandLine(new CheckSum()).execute(args);
            System.exit(exitCode);
        }

        @Override
        public Integer call()
                throws Exception
        { // your business logic goes here...
            byte[] fileContents = Files.readAllBytes(file.toPath());
            byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
            System.out.printf("%0" + (digest.length * 2) + "x%n", new BigInteger(1, digest));
            return 0;
        }
    }

    public static void main(String[] args)
            throws Throwable
    {
        Bootstrap.bootstrap();
        new CommandLine(new CheckSum()).execute(args);
    }
}
