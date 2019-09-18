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
package com.wrmsr.tokamak.main.bootstrap;

import com.wrmsr.tokamak.main.bootstrap.dns.Dns;

import java.io.InputStreamReader;

public final class Bootstrap
{
    private Bootstrap()
    {
    }

    public interface Op
    {
        void run()
                throws Exception;
    }

    public static final String PAUSE_PROPERTY_KEY = "com.wrmsr.tokamak.main.pause";

    public static final class PauseOp
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

    public static final class SetHeadlessOp
            implements Op
    {
        @Override
        public void run()
        {
            System.setProperty("apple.awt.UIElement", "true");
            System.setProperty("java.awt.headless", "true");
        }
    }

    public static final class FixDnsOp
            implements Op
    {
        @Override
        public void run()
                throws Exception
        {
            Dns.fixPosixLocalhostHostsFile();
        }
    }

    private static final Object lock = new Object();
    private static volatile boolean hasRun = false;

    public static void bootstrap()
    {
        if (!hasRun) {
            synchronized (lock) {
                if (!hasRun) {
                    try {
                        doBootstrap();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    hasRun = true;
                }
            }
        }
    }

    private static void doBootstrap()
            throws Exception
    {
        for (Op op : new Op[] {
                new PauseOp(),
                new SetHeadlessOp(),
                new FixDnsOp(),
        }) {
            op.run();
        }
    }
}
