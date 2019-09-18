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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wrmsr.tokamak.main.util.OS;
import com.wrmsr.tokamak.util.Logger;
import com.wrmsr.tokamak.util.lifecycle.LifecycleManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import static com.wrmsr.tokamak.util.lifecycle.Lifecycles.runLifecycle;

public class ServerMain
{
    private static final Logger log = Logger.get(ServerMain.class);

    // cd tokamak-main/target && tar xvf tokamak-main-0.1-SNAPSHOT.tar.gz && cd tokamak-main-0.1-SNAPSHOT

    // --add-opens java.base/java.lang=ALL-UNNAMED

    public static LoggerContext configureLogging()
    {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.DEBUG);
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
        builder.add(builder.newRootLogger(Level.DEBUG).add(builder.newAppenderRef("Stdout")));
        return Configurator.initialize(builder.build());
    }

    public static void main(String[] args)
            throws Exception
    {
        System.setProperty("apple.awt.UIElement", "true");
        System.setProperty("java.awt.headless", "true");

        configureLogging();

        log.info(OS.get().toString());

        Injector injector = Guice.createInjector(new ServerModule());
        runLifecycle(injector.getInstance(LifecycleManager.class), () -> {
            Thread.sleep(600000);
        });
    }
}
