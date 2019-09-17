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
package com.wrmsr.tokamak.main.server;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.inject.Injector;
import com.wrmsr.tokamak.util.Logger;
import com.wrmsr.tokamak.util.lifecycle.AbstractLifecycle;
import me.bazhenov.groovysh.GroovyShellService;

import javax.inject.Inject;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class GshService
        extends AbstractLifecycle
{
    private static final Logger log = Logger.get(GshService.class);

    private final Injector injector;

    private GroovyShellService service;

    @Inject
    public GshService(Injector injector)
    {
        this.injector = injector;
    }

    @Override
    protected void doStart()
            throws Exception
    {
        System.setProperty("groovysh.prompt", "tokamak");
        service = new GroovyShellService();
        service.setPort(6789);

        service.setBindings(ImmutableMap.of(
                "injector", injector
        ));

        String banner = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/com/wrmsr/tokamak/main/banner.txt")));
        String bannerLit = ("\n" + banner + "\n\n").replace("\n", "\\n");

        Path tempDir = Files.createTempDirectory("tokamak-main");
        tempDir.toFile().deleteOnExit();
        Path out = tempDir.resolve("out.groovy");
        Files.write(out, ("print(\"" + bannerLit + "\")").getBytes(Charsets.UTF_8));
        service.addDefaultScript(out.toFile().getAbsolutePath());

        service.start();
        log.info("Started GroovyShell");
    }

    @Override
    protected void doStop()
            throws Exception
    {
        service.destroy();
        log.info("Stopped GroovyShell");
    }
}
