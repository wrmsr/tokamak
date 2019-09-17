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
package com.wrmsr.tokamak.main;

import com.wrmsr.tokamak.main.util.pid.Jdk9Pids;
import com.wrmsr.tokamak.main.util.pid.Pids;

public class ShutdownHook
{
    private static Thread main;

    public static void main(String[] a)
            throws Exception
    {
        Pids pids;

        // pids = new JnaPids();
        pids = new Jdk9Pids();

        int pid = pids.get();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            public void run()
            {
                System.out.println("TERM");
                main.interrupt();
                for (int i = 0; i < 4; i++) {
                    System.out.println("busy");
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception e) {}
                }
                System.out.println("exit");
            }
        }));

        main = Thread.currentThread();

        while (true) {
            Thread.sleep(1000);
            System.out.println(String.format("pid: %d", pid));
        }
    }
}
