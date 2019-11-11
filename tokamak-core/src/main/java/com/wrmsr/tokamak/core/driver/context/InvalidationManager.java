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
package com.wrmsr.tokamak.core.driver.context;

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.context.state.StateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PState;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class InvalidationManager
{
    private final Plan plan;
    private final StateCache stateCache;

    public InvalidationManager(
            Plan plan,
            StateCache stateCache)
    {
        this.plan = checkNotNull(plan);
        this.stateCache = checkNotNull(stateCache);
    }

    public void invalidate(State state)
    {
        checkNotNull(state);

        if (state.getLinkage() != null) {
            state.getLinkage().getOutput().forEach((sinkNodeId, links) -> {
                PState sinkNode = (PState) checkNotNull(plan.getNode(sinkNodeId));
                stateCache.invalidate(sinkNode, links.getIds(
            });
        }
    }

    public void recursiveInvalidate(
            Map<PState, Set<Id>> idSetsByNode,
            boolean input,
            boolean output)
    {
        throw new IllegalStateException();
    }
}
