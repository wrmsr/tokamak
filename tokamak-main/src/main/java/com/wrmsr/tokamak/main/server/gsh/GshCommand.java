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

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.session.helpers.AbstractSession;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static com.wrmsr.tokamak.main.server.gsh.GshService.SHELL_KEY;
import static java.util.Arrays.asList;

public class GshCommand
        implements Command, SessionAware
{
    private final SshServer sshd;
    private final Map<String, Object> bindings;
    private final List<String> defaultScripts;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Thread wrapper;
    private ServerSession session;

    public GshCommand(SshServer sshd, Map<String, Object> bindings, List<String> defaultScripts)
    {
        this.sshd = sshd;
        this.bindings = bindings;
        this.defaultScripts = defaultScripts;
    }

    @Override
    public void setSession(ServerSession session)
    {
        this.session = session;
    }

    @Override
    public void setInputStream(InputStream in)
    {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out)
    {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err)
    {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public void start(ChannelSession channel, Environment env)
    {
        TtyFilterOutputStream out = new TtyFilterOutputStream(this.out);
        TtyFilterOutputStream err = new TtyFilterOutputStream(this.err);

        IO io = new IO(in, out, err);
        io.setVerbosity(IO.Verbosity.DEBUG);
        final Groovysh shell = new Groovysh(createBinding(bindings, out, err), io);

        @SuppressWarnings({"rawtypes"})
        Closure errorHook = new Closure(this)
        {
            @Override
            public Object call(Object... args)
            {
                if (args[0] instanceof InterruptedIOException || args[0] instanceof SshException) {
                    // Stopping groovysh thread in case of broken client channel
                    shell.getRunner().setRunning(false);
                }
                return shell.getDefaultErrorHook().call(args);
            }
        };
        shell.setErrorHook(errorHook);

        try {
            loadDefaultScripts(shell);
        }
        catch (Exception e) {
            createPrintStream(err).println("Unable to load default scripts: "
                    + e.getClass().getName() + ": " + e.getMessage());
        }

        session.setAttribute(SHELL_KEY, shell);

        String threadName = "GroovySh Client Thread: " + session.getIoSession().getRemoteAddress().toString();
        wrapper = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    SshTerminal.registerEnvironment(env);
                    shell.run("");
                    callback.onExit(0);
                }
                catch (RuntimeException | Error e) {
                    callback.onExit(-1, e.getMessage());
                }
            }
        }, threadName);

        wrapper.start();
    }

    private Binding createBinding(Map<String, Object> objects, OutputStream out, OutputStream err)
    {
        Binding binding = new Binding();

        if (objects != null) {
            for (Map.Entry<String, Object> row : objects.entrySet()) {
                binding.setVariable(row.getKey(), row.getValue());
            }
        }

        binding.setVariable("out", createPrintStream(out));
        binding.setVariable("err", createPrintStream(err));

        binding.setVariable("activeSessions", new Closure<List<AbstractSession>>(this)
        {
            private static final long serialVersionUID = -5067603783691580051L;

            @Override
            public List<AbstractSession> call()
            {
                return sshd.getActiveSessions();
            }
        });

        return binding;
    }

    private static PrintStream createPrintStream(OutputStream out)
    {
        try {
            return new PrintStream(out, true, "utf8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "serial"})
    private void loadDefaultScripts(final Groovysh shell)
    {
        if (!defaultScripts.isEmpty()) {
            Closure<Groovysh> defaultResultHook = shell.getResultHook();

            try {
                // Set a "no-op closure so we don't get per-line value output when evaluating the default script
                shell.setResultHook(new Closure<Groovysh>(this)
                {
                    @Override
                    public Groovysh call(Object... args)
                    {
                        return shell;
                    }
                });

                org.codehaus.groovy.tools.shell.Command cmd = shell.getRegistry().find(":load");
                for (String script : defaultScripts) {
                    cmd.execute(asList(script));
                }
            }
            finally {
                // Restoring original result hook
                shell.setResultHook(defaultResultHook);
            }
        }
    }

    @Override
    public void destroy(ChannelSession channel)
            throws Exception
    {
        wrapper.interrupt();
    }
}
