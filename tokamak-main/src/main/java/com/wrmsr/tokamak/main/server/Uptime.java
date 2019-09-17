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

import com.wrmsr.tokamak.util.lifecycle.AbstractLifecycle;

import java.lang.management.ManagementFactory;

public final class Uptime
{
    private Uptime()
    {
    }

    public static long getJvmUptime()
    {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    public static final class Service
            extends AbstractLifecycle
    {
        private long startTimeMillis;
        private long stopTimeMillis;

        public long getStartTimeMillis()
        {
            return startTimeMillis;
        }

        public long getStopTimeMillis()
        {
            return stopTimeMillis;
        }

        public long getUptimeMillis()
        {
            return startTimeMillis != 0 ? System.currentTimeMillis() - startTimeMillis : 0;
        }

        @Override
        protected void doStart()
                throws Exception
        {
            startTimeMillis = System.currentTimeMillis();
        }

        @Override
        protected void doStop()
                throws Exception
        {
            stopTimeMillis = System.currentTimeMillis();
        }
    }
}
