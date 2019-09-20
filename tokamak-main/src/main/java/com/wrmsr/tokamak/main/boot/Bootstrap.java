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
package com.wrmsr.tokamak.main.boot;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.main.boot.dns.Dns;
import com.wrmsr.tokamak.main.jna.JnaExec;
import com.wrmsr.tokamak.main.util.exec.Exec;
import com.wrmsr.tokamak.util.Jdk;
import com.wrmsr.tokamak.util.config.Compilation;
import com.wrmsr.tokamak.util.config.ConfigMetadata;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import javax.annotation.CheckReturnValue;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class Bootstrap
{
    /*
    TODO:
     - rlimits
     - jdk selection
    */

    private final BootstrapConfig config;
    private final Class<?> mainCls;
    private final String[] args;

    private Bootstrap(BootstrapConfig config, Class<?> mainCls, String[] args)
    {
        this.config = checkNotNull(config);
        this.mainCls = checkNotNull(mainCls);
        this.args = checkNotNull(args);
    }

    public BootstrapConfig getConfig()
    {
        return config;
    }

    public Class<?> getMainCls()
    {
        return mainCls;
    }

    public String[] getArgs()
    {
        return args;
    }

    public interface Op
    {
        void run()
                throws Exception;
    }

    public static final String PAUSE_PROPERTY_KEY = "com.wrmsr.tokamak.main.pause";

    public final class PauseOp
            implements Op
    {
        @Override
        public void run()
                throws Exception
        {
            if (!System.getProperty(PAUSE_PROPERTY_KEY, "").isEmpty()) {
                try (InputStreamReader isr = new InputStreamReader(System.in)) {
                    while (isr.read() != '\n') {}
                }
            }
        }
    }

    public final class SetHeadlessOp
            implements Op
    {
        @Override
        public void run()
        {
            System.setProperty("apple.awt.UIElement", "true");
            System.setProperty("java.awt.headless", "true");
        }
    }

    public final class FixDnsOp
            implements Op
    {
        @Override
        public void run()
                throws Exception
        {
            if (Jdk.getMajor() > 8) {
                Dns.fixPosixLocalhostHostsFile();
            }
            else {
                Dns.hookDnsToFixLocalhost();
            }
        }
    }

    public final class ConfigureLoggingOp
            implements Op
    {
        @Override
        public void run()
                throws Exception
        {
            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

            ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

            builder.setStatusLevel(Level.WARN);

            builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                    .addAttribute("level", Level.INFO));

            AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
                    ConsoleAppender.Target.SYSTEM_OUT);
            appenderBuilder.add(builder.newLayout("PatternLayout")
                    .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
            appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
                    .addAttribute("marker", "FLOW"));

            builder.add(appenderBuilder);

            builder.add(builder.newLogger("org.apache.logging.log4j", Level.INFO)
                    .add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));

            builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("Stdout")));

            Configurator.initialize(builder.build());
        }
    }

    public static final String NO_REEXEC_PROPERTY_KEY = "com.wrmsr.tokamak.main.boot.noreexec";

    public final class ReexecOp
            implements Op
    {
        public File getJvm()
        {
            File jvm = Paths.get(
                    System.getProperty("java.home"),
                    "bin",
                    "java" + (System.getProperty("os.name").startsWith("Win") ? ".exe" : "")
            ).toFile();
            checkState(jvm.exists(), "cannot find jvm: " + jvm.getAbsolutePath());
            checkState(jvm.isFile(), "jvm is not a file: " + jvm.getAbsolutePath());
            checkState(jvm.canExecute(), "jvm is not executable: " + jvm.getAbsolutePath());
            return jvm;
        }

        @Override
        public void run()
                throws Exception
        {
            if (System.getProperties().containsKey(NO_REEXEC_PROPERTY_KEY)) {
                return;
            }

            // FIXME: check if debugging

            Exec exec;

            exec = new JnaExec();
            // exec = new ProcessBuilderExec();

            String jvm = getJvm().getAbsolutePath();
            String cp = System.getProperty("java.class.path");
            String main = mainCls.getCanonicalName();

            exec.exec(jvm, ImmutableList.<String>builder().add("-cp", cp, "-D" + NO_REEXEC_PROPERTY_KEY, main).add(args).build());
        }
    }

    private void run()
            throws Exception
    {
        for (Op op : new Op[] {
                new PauseOp(),
                new SetHeadlessOp(),
                new FixDnsOp(),
                new ConfigureLoggingOp(),
                new ReexecOp(),
        }) {
            op.run();
        }
    }

    private static final Object LOCK = new Object();
    private static volatile Bootstrap INSTANCE = null;

    @CheckReturnValue
    public static String[] bootstrap(BootstrapConfig config, Class<?> mainCls, String[] args)
    {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    Bootstrap instance;
                    try {
                        instance = new Bootstrap(config, mainCls, args);
                        instance.run();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    INSTANCE = instance;
                }
            }
        }
        return args;
    }

    @CheckReturnValue
    @SuppressWarnings({"unchecked"})
    public static String[] bootstrap(Class<?> mainCls, String[] args)
    {
        Class<? extends BootstrapConfig> bcImpl;
        try {
            bcImpl = (Class<? extends BootstrapConfig>) Class.forName(Compilation.getCompiledImplName(BootstrapConfig.class));
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Compilation.ImplFactory<BootstrapConfig> bcFac = Compilation.getImplFactory(bcImpl);
        BootstrapConfig bc = bcFac.build(new ConfigMetadata(BootstrapConfig.class));
        return bootstrap(bc, mainCls, args);
    }
}
