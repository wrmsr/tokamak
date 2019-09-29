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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.context.state.StateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.StateNode;

import javax.annotation.concurrent.Immutable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class GetStateBuildOp
        implements BuildOp
{
    private final StateNode node;
    private final Id id;
    private final EnumSet<StateCache.GetFlag> flags;
    private final Consumer<Optional<State>> callback;

    public GetStateBuildOp(StateNode node, Id id, EnumSet<StateCache.GetFlag> flags, Consumer<Optional<State>> callback)
    {
        this.node = checkNotNull(node);
        this.id = checkNotNull(id);
        this.flags = checkNotNull(flags);
        this.callback = checkNotNull(callback);
    }

    public StateNode getNode()
    {
        return node;
    }

    public Id getId()
    {
        return id;
    }

    public EnumSet<StateCache.GetFlag> getFlags()
    {
        return flags;
    }

    public Consumer<Optional<State>> getCallback()
    {
        return callback;
    }
}
