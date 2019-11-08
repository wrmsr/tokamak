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
package com.wrmsr.tokamak.main.server.jaxrs;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.Application;

import java.util.Set;

public class ApplicationModule
        extends AbstractModule
{
    @Provides
    public Application provideApplication(Injector injector, @Resource Set<Class<?>> resources)
    {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(GuiceFeature.class);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(ObjectMapperProvider.class);
        resources.forEach(resourceConfig::register);

        GuiceFeature.setApplicationInjector(resourceConfig, injector);

        return resourceConfig;
    }
}
