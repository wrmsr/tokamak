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

import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.SingleSourceNode;
import com.wrmsr.tokamak.util.MorePreconditions;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public abstract class SingleSourceBuilder<T extends SingleSourceNode>
        extends AbstractBuilder<T>
{
    protected final Builder source;

    public SingleSourceBuilder(DriverImpl driver, T node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
        Builder source = MorePreconditions.checkSingle(this.sources.values());
        checkArgument(source.getNode() == node.getSource());
        this.source = source;
    }

    public Builder getSource()
    {
        return source;
    }
}
