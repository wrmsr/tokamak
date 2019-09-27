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
import com.wrmsr.tokamak.main.boot.Bootstrap;
import com.wrmsr.tokamak.util.Logger;
import com.wrmsr.tokamak.util.lifecycle.LifecycleManager;

import static com.wrmsr.tokamak.util.lifecycle.Lifecycles.runLifecycle;

public class ServerMain
{
    private static final Logger log = Logger.get(ServerMain.class);

    public static void main(String[] args)
            throws Exception
    {
        args = Bootstrap.bootstrap(ServerMain.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            public void run()
            {
                System.out.println("TERM");
            }
        }));

        Injector injector = Guice.createInjector(new ServerModule());
        runLifecycle(injector.getInstance(LifecycleManager.class), () -> {
            Thread.sleep(600000);
        });
    }
}
