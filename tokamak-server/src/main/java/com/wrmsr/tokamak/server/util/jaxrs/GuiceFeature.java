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
package com.wrmsr.tokamak.server.util.jaxrs;

import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import java.util.Map;
import java.util.WeakHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class GuiceFeature
        implements Feature
{
    private final Application application;

    @Inject
    public GuiceFeature(Application application)
    {
        this.application = checkNotNull(application);
    }

    private static final Object lock = new Object();
    private static final Map<Application, Injector> injectorsByApplication = new WeakHashMap<>();

    public static void setApplicationInjector(Application application, Injector injector)
    {
        synchronized (lock) {
            injectorsByApplication.put(application, injector);
        }
    }

    public static Injector getApplicationInjector(Application application)
    {
        synchronized (lock) {
            return injectorsByApplication.get(application);
        }
    }

    public static void unsetApplicationInjector(Application application)
    {
        synchronized (lock) {
            injectorsByApplication.remove(application);
        }
    }

    @Override
    public boolean configure(FeatureContext context)
    {
        Injector injector = checkNotNull(getApplicationInjector(application));
        InjectionManager injectionManager = InjectionManagerProvider.getInjectionManager(context);
        install(injector, injectionManager);
        return true;
    }

    public static void install(Injector injector, InjectionManager injectionManager)
    {
        ServiceLocator serviceLocator;
        try {
            serviceLocator = (ServiceLocator) injectionManager.getClass().getDeclaredMethod("getServiceLocator")
                    .invoke(injectionManager);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(injector);
    }
}
