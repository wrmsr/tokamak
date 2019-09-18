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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.inject.Injector;
import com.wrmsr.tokamak.util.Logger;
import com.wrmsr.tokamak.util.lifecycle.AbstractLifecycle;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ShellFactory;
import org.codehaus.groovy.tools.shell.Groovysh;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.wrmsr.tokamak.util.MoreFiles.writeTempFile;
import static java.util.concurrent.TimeUnit.HOURS;
import static jline.TerminalFactory.Flavor.UNIX;
import static jline.TerminalFactory.registerFlavor;
import static org.apache.sshd.common.FactoryManager.IDLE_TIMEOUT;
import static org.apache.sshd.server.SshServer.setUpDefaultServer;

public class GshService
        extends AbstractLifecycle
{
    private static final Logger log = Logger.get(GshService.class);

    private final Injector injector;

    public static final Session.AttributeKey<Groovysh> SHELL_KEY = new Session.AttributeKey<>();

    private int port = 6789;
    private String host;
    private Map<String, Object> bindings;
    private PasswordAuthenticator passwordAuthenticator;

    private List<String> defaultScripts = new ArrayList<>();
    private SshServer sshd;

    @Inject
    public GshService(Injector injector)
    {
        this.injector = injector;
    }

    @Override
    protected void doStart()
            throws Exception
    {
        registerFlavor(UNIX, SshTerminal.class);

        System.setProperty("groovysh.prompt", "tokamak");

        bindings = ImmutableMap.of(
                "injector", injector
        );

        String banner = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/com/wrmsr/tokamak/main/banner.txt")));
        String bannerLit = ("\n" + banner + "\n\n").replace("\n", "\\n");

        Path out = writeTempFile("boot.groovy", ("print(\"" + bannerLit + "\")").getBytes(Charsets.UTF_8));
        defaultScripts.add(out.toFile().getAbsolutePath());

        sshd = buildSshServer();
        sshd.start();
        log.info("Started GroovyShell");
    }

    @Override
    protected void doStop()
            throws Exception
    {
        sshd.stop(true);
        log.info("Stopped GroovyShell");
    }

    protected SshServer buildSshServer()
    {
        SshServer sshd = setUpDefaultServer();
        sshd.setPort(port);
        if (host != null) {
            sshd.setHost(host);
        }

        PropertyResolverUtils.updateProperty(sshd, IDLE_TIMEOUT, HOURS.toMillis(1));

        sshd.addSessionListener(new SessionListener()
        {
            @Override
            public void sessionCreated(Session session)
            {
            }

            @Override
            public void sessionEvent(Session sesssion, Event event)
            {
            }

            @Override
            public void sessionException(Session session, Throwable t)
            {
            }

            @Override
            public void sessionClosed(Session session)
            {
                Groovysh shell = session.getAttribute(SHELL_KEY);
                if (shell != null) {
                    shell.getRunner().setRunning(false);
                }
            }
        });

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.key").toPath()));
        configureAuthentication(sshd);
        sshd.setShellFactory(new GroovyShellFactory());
        return sshd;
    }

    private void configureAuthentication(SshServer sshd)
    {
        NamedFactory<UserAuth> auth;
        if (this.passwordAuthenticator != null) {
            sshd.setPasswordAuthenticator(this.passwordAuthenticator);
            auth = new UserAuthPasswordFactory();
        }
        else {
            auth = new UserAuthNoneFactory();
        }
        sshd.setUserAuthFactories(Collections.singletonList(auth));
    }

    private class GroovyShellFactory
            implements ShellFactory
    {
        @Override
        public Command createShell(ChannelSession channel)
                throws IOException
        {
            return new GshCommand(sshd, bindings, defaultScripts);
        }
    }
}
