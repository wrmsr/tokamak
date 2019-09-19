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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskThread
        extends Thread
{
    private static final Logger log = Logger.get(TaskThread.class);

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Notifier notifier;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final Runnable runnable;

    public TaskThread(String name, Duration interval, Runnable runnable)
    {
        super(name);
        setDaemon(true);
        this.notifier = new Notifier(interval);
        this.runnable = runnable;
    }

    public static final class Notifier
    {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private volatile Duration timeout;

        public Notifier(Duration timeout)
        {
            assert timeout != null;
            this.timeout = timeout;
        }

        public void await()
        {
            lock.lock();
            try {
                condition.await(timeout.get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                Thread.interrupted();
            }
            finally {
                lock.unlock();
            }
        }

        public void setTimeout(Duration timeout)
        {
            assert timeout != null;
            this.timeout = timeout;
            doNotify();
        }

        public Duration getTimeout()
        {
            return timeout;
        }

        public void doNotify()
        {
            lock.lock();
            try {
                condition.signalAll();
            }
            finally {
                lock.unlock();
            }
        }
    }

    public void shutdown()
            throws InterruptedException
    {
        if (running.compareAndSet(true, false)) {
            notifier.doNotify();
            shutdownLatch.await();
        }
    }

    public void resetInterval(Duration interval)
    {
        notifier.setTimeout(interval);
    }

    public void run()
    {
        try {
            while (running.get()) {
                try {
                    runnable.run();
                }
                catch (Throwable e) {
                    if (running.get()) {
                        log.warn("Failed to execute runnable task", e);
                    }
                }
                if (running.get()) {
                    notifier.await();
                }
            }
        }
        finally {
            shutdownLatch.countDown();
        }
    }

    public Duration getInterval()
    {
        return notifier.getTimeout();
    }
}
