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
package com.wrmsr.tokamak.core.driver.build.ops;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.context.state.StateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.PState;

import javax.annotation.concurrent.Immutable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class GetStateBuildOp
        extends AbstractBuildOp
{
    private final PState node;
    private final Set<Id> ids;
    private final EnumSet<StateCache.GetFlag> flags;
    private final Consumer<Map<Id, State>> callback;

    public GetStateBuildOp(Builder<?> origin, PState node, Set<Id> ids, EnumSet<StateCache.GetFlag> flags, Consumer<Map<Id, State>> callback)
    {
        super(origin);
        this.node = checkNotNull(node);
        this.ids = ImmutableSet.copyOf(ids);
        this.flags = checkNotNull(flags);
        this.callback = checkNotNull(callback);
    }

    @Override
    public String toString()
    {
        return "GetStateBuildOp{" +
                "origin=" + origin +
                ", node=" + node +
                ", ids=" + ids +
                ", flags=" + flags +
                '}';
    }

    public PState getNode()
    {
        return node;
    }

    public Set<Id> getIds()
    {
        return ids;
    }

    public EnumSet<StateCache.GetFlag> getFlags()
    {
        return flags;
    }

    public Consumer<Map<Id, State>> getCallback()
    {
        return callback;
    }
}
