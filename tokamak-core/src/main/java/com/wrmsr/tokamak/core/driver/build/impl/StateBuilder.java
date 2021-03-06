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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.GetStateBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.driver.context.state.StateCache;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.serde.Serde;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class StateBuilder
        extends SingleSourceBuilder<PState>
        implements IdNodeBuilder<PState>
{
    private final Set<String> idFields;
    private final List<String> orderedIdFields;
    private final Serde<Object[]> idSerde;

    public StateBuilder(DriverImpl driver, PState node, Map<PNode, Builder<?>> sources)
    {
        super(driver, node, sources);

        orderedIdFields = getOrderedIdFields();
        idFields = getIdFields();
        idSerde = getIdSerde();
    }

    @Override
    protected void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        // FIXME: supersets
        if (key.getFields().equals(idFields)) {
            Id id = IdNodeBuilder.newId(key, idFields, orderedIdFields, idSerde);

            opConsumer.accept(new GetStateBuildOp(this, node, ImmutableSet.of(id), EnumSet.noneOf(StateCache.GetFlag.class), statesById -> {
                if (!statesById.isEmpty()) {
                    State state = checkSingle(statesById.values());

                    DriverRow row = new DriverRow(
                            node,
                            context.getDriver().getLineagePolicy().build(),
                            id,
                            state.getAttributes());

                    opConsumer.accept(new ResponseBuildOp(this, key, ImmutableList.of(row)));
                }
                else {
                    innerBuildMiss(context, key, opConsumer);
                }
            }));
        }
        else {
            innerBuildMiss(context, key, opConsumer);
        }
    }

    protected void innerBuildMiss(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        opConsumer.accept(new RequestBuildOp(this, source, key, map -> {
            List<DriverRow> srows = checkSingle(checkSingle(map.values()).values());
            checkNotEmpty(srows);
            DriverRow firstRow = srows.iterator().next();
            if (firstRow.getId() == null) {
                checkState(srows.size() == 1);
                checkState(firstRow.isEmpty());

                DriverRow emptyRow = new DriverRow(
                        node,
                        context.getDriver().getLineagePolicy().build(firstRow),
                        null,
                        null);

                opConsumer.accept(new ResponseBuildOp(this, key, ImmutableList.of(emptyRow)));
            }

            Set<Id> ids = new HashSet<>();
            for (DriverRow row : srows) {
                ids.add(checkNotNull(row.getId()));
            }

            opConsumer.accept(new GetStateBuildOp(this, node, ids, EnumSet.of(StateCache.GetFlag.CREATE), statesById -> {
                ImmutableList.Builder<DriverRow> builder = ImmutableList.builder();

                for (DriverRow row : srows) {
                    State state = checkNotNull(statesById.get(row.getId()));
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
        }));
    }
}
