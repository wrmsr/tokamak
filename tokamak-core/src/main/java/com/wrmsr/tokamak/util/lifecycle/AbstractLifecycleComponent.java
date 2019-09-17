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
package com.wrmsr.tokamak.util.lifecycle;

public abstract class AbstractLifecycleComponent
        implements LifecycleComponent
{
    private final LifecycleController lifecycleController;

    private final class Delegate
            implements LifecycleComponent
    {
        @Override
        public String toString()
        {
            return "Delegate{target=" + AbstractLifecycleComponent.this + "}";
        }

        @Override
        public void construct()
                throws Exception
        {
            doConstruct();
        }

        @Override
        public void start()
                throws Exception
        {
            doStart();
        }

        @Override
        public void stop()
                throws Exception
        {
            doStop();
        }

        @Override
        public void destroy()
                throws Exception
        {
            doDestroy();
        }
    }

    public AbstractLifecycleComponent()
    {
        lifecycleController = new LifecycleController(new Delegate());
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "{" +
                "lifecycleState=" + lifecycleController.getState() +
                '}';
    }

    public LifecycleController getLifecycleController()
    {
        return lifecycleController;
    }

    public LifecycleState getLifecycleState()
    {
        return lifecycleController.getState();
    }

    public boolean isStarted()
    {
        return getLifecycleState().isStarted();
    }

    public boolean isStopped()
    {
        return getLifecycleState().isStopped();
    }

    @Override
    public final void construct()
            throws Exception
    {
        lifecycleController.construct();
    }

    protected void doConstruct()
            throws Exception
    {
    }

    @Override
    public final void start()
            throws Exception
    {
        lifecycleController.start();
    }

    protected void doStart()
            throws Exception
    {
    }

    @Override
    public final void stop()
            throws Exception
    {
        lifecycleController.stop();
    }

    protected void doStop()
            throws Exception
    {
    }

    @Override
    public final void destroy()
            throws Exception
    {
        lifecycleController.destroy();
    }

    protected void doDestroy()
            throws Exception
    {
    }
}
