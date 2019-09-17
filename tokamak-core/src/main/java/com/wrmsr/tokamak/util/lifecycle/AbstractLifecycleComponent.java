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

    public AbstractLifecycleComponent()
    {
        lifecycleController = new LifecycleController(new LifecycleComponent()
        {
            @Override
            public void postConstruct()
                    throws Exception
            {
                doPostConstruct();
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
            public void close()
                    throws Exception
            {
                doClose();
            }
        });
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
    public final void postConstruct()
            throws Exception
    {
        lifecycleController.postConstruct();
    }

    protected void doPostConstruct()
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
    public final void close()
            throws Exception
    {
        lifecycleController.close();
    }

    protected void doClose()
            throws Exception
    {
    }
}
