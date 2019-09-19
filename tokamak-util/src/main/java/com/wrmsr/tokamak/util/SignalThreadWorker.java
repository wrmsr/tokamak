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
package com.wrmsr.tokamak.util;

import java.util.concurrent.locks.LockSupport;

public abstract class SignalThreadWorker
{
    public final long sleepMillis;
    public final Thread thread;

    protected final Object sleepObject = new Object();
    protected volatile boolean shouldRun = true;

    protected SignalThreadWorker(long sleepMillis)
    {
        this.sleepMillis = sleepMillis;
        thread = new Thread()
        {
            @Override
            public void run()
            {
                threadProc();
            }
        };
    }

    protected SignalThreadWorker()
    {
        this(0);
    }

    public void start()
    {
        thread.start();
    }

    public boolean isAlive()
    {
        return thread.isAlive();
    }

    protected void threadProc()
    {
        while (shouldRun) {
            LockSupport.park();
            onSignal();
        }
    }

    protected abstract void onSignal();

    protected void sleep()
    {
        if (sleepMillis < 1) {
            return;
        }
        try {
            synchronized (sleepObject) {
                sleepObject.wait(sleepMillis);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void signal()
    {
        LockSupport.unpark(thread);
    }

    public synchronized void stop(long millis)
    {
        if (!thread.isAlive()) {
            return;
        }
        shouldRun = false;
        LockSupport.unpark(thread);
        synchronized (sleepObject) {
            sleepObject.notifyAll();
        }
        try {
            thread.join(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop()
    {
        stop(0);
    }
}
