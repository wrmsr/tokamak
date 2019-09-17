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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.wrmsr.tokamak.main.server.util.jaxrs.ApplicationModule;
import com.wrmsr.tokamak.main.server.util.jaxrs.NettyServer;
import com.wrmsr.tokamak.main.server.util.jaxrs.Resource;
import com.wrmsr.tokamak.util.Json;
import com.wrmsr.tokamak.util.lifecycle.LifecycleModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class ServerModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        binder.install(new LifecycleModule());

        binder.bind(ObjectMapper.class).toInstance(Json.newObjectMapper());
        binder.install(new ApplicationModule());
        binder.bind(NettyServer.class).asEagerSingleton();
        newSetBinder(binder, Class.class, Resource.class).addBinding().toInstance(RootResource.class);
    }
}
