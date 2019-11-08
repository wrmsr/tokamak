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
package com.wrmsr.tokamak.core.driver.build.impl;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractBuilder<T extends PNode>
        implements Builder<T>
{
    protected final DriverImpl driver;
    protected final T node;
    protected final Map<PNode, Builder<?>> sources;

    public AbstractBuilder(DriverImpl driver, T node, Map<PNode, Builder<?>> sources)
    {
        this.driver = checkNotNull(driver);
        this.node = checkNotNull(node);
        this.sources = ImmutableMap.copyOf(sources);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "node=" + node +
                '}';
    }

    @Override
    public DriverImpl getDriver()
    {
        return driver;
    }

    @Override
    public T getNode()
    {
        return node;
    }

    @Override
    public Map<PNode, Builder<?>> getSources()
    {
        return sources;
    }

    public final void build(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        checkNotNull(context);
        checkNotNull(key);
        checkArgument(context.getDriver() == driver);
        innerBuild(context, key, opConsumer);
    }

    protected abstract void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer);
}
