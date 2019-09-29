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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.driver.context.state.StateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.StateNode;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;

public final class StateBuilder
        extends SingleSourceBuilder<StateNode>
{
    public StateBuilder(DriverImpl driver, StateNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        /*
        if (builder.getNode() instanceof StateNode && key instanceof IdKey) {
            StateNode statefulNode = (StateNode) builder.getNode();
            IdKey idKey = (IdKey) key;
            Optional<State> stateOpt = stateCache.get(statefulNode, idKey.getId(), EnumSet.of(StateCache.GetFlag.CREATE));
            if (stateOpt.isPresent()) {
                State state = stateOpt.get();
                checkState(state.getId().equals(idKey.getId()));
                checkState(!state.getMode().isStorageMode());
                if (state.getMode() != State.Mode.INVALID) {
                    DriverRow row = new DriverRow(
                            statefulNode,
                            driver.getLineagePolicy().build(),
                            state.getId(),
                            state.getAttributes());
                    if (journaling) {
                        addJournalEntry(new JournalEntry.StateCachedBuildOutput(node, key, ImmutableList.of(row), state));
                    }
                }
            }
        }
        */

        opConsumer.accept(new RequestBuildOp(this, source, key, srows -> {
            ImmutableList.Builder<DriverRow> builder = ImmutableList.builder();

            for (DriverRow row : srows) {
                if (row.getId() == null) {
                    checkState(row.isNull());
                    continue;
                }

                Optional<State> stateOpt = context.getStateCache().get(node, row.getId(), EnumSet.of(StateCache.GetFlag.CREATE));
                State state = stateOpt.get();
                if (state.getMode() == State.Mode.INVALID) {
                    state.setAttributes(row.getAttributes());
                }

                context.getLinkageManager().addStateLineage(state, row.getLineage());

                builder.add(
                        new DriverRow(
                                node,
                                context.getDriver().getLineagePolicy().build(row),
                                row.getId(),
                                row.getAttributes()));
            }

            opConsumer.accept(new ResponseBuildOp(this, key, builder.build()));
        }));
    }
}