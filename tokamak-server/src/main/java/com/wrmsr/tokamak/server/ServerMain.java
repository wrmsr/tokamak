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

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.channel.Channel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.netty.httpserver.TokamakNettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;

public class ServerMain
{
    public static LoggerContext configureLogging()
    {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR);
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                .addAttribute("level", Level.DEBUG));
        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
                ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
                .addAttribute("marker", "FLOW"));
        builder.add(appenderBuilder);
        builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
                .add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("Stdout")));
        return Configurator.initialize(builder.build());
    }

    public static void main(String[] args)
            throws InterruptedException
    {
        configureLogging();

        Injector injector = Guice.createInjector();

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        ResourceConfig resourceConfig = new ResourceConfig(RootResource.class);
        Channel server = TokamakNettyHttpContainerProvider.createServer(baseUri, resourceConfig, null, ah -> {
            ServiceLocator serviceLocator;
            try {
                InjectionManager injectionManager = ah.getInjectionManager();
                serviceLocator = (ServiceLocator) injectionManager.getClass().getDeclaredMethod("getServiceLocator")
                        .invoke(injectionManager);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
            GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
            guiceBridge.bridgeGuiceInjector(injector);
        });
        server.closeFuture().sync();

        // server.run();
    }
}
