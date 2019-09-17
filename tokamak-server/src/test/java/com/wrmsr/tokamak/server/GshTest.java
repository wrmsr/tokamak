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
package com.wrmsr.tokamak.server;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import me.bazhenov.groovysh.GroovyShellService;
import org.junit.Test;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class GshTest
{
    // ssh -o "StrictHostKeyChecking no" -o "UserKnownHostsFile /dev/null" 127.1 -p 6789

    @Test
    public void testGsh()
            throws Throwable
    {
        GroovyShellService service = new GroovyShellService();
        service.setPort(6789);
        service.setBindings(ImmutableMap.of(
                "foo", 420,
                "bar", "bong"
        ));

        String banner = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("banner.txt")));
        String bannerLit = ("\n" + banner + "\n\n").replace("\n", "\\n");

        Path tempDir = Files.createTempDirectory("tokamak-server");
        tempDir.toFile().deleteOnExit();
        Path out = tempDir.resolve("out.groovy");
        Files.write(out, ("print(\"" + bannerLit + "\")").getBytes(Charsets.UTF_8));
        service.addDefaultScript(out.toFile().getAbsolutePath());

        service.start();
        Thread.currentThread().sleep(600000);
        System.out.println("intentionally dying");
        service.destroy();
    }
}
