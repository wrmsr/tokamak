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
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.BuilderContext;
import com.wrmsr.tokamak.core.driver.build.ContextualBuilder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CacheBuilder
        extends SingleSourceBuilder<PCache>
        implements ContextualBuilder<PCache>
{
    public CacheBuilder(DriverImpl driver, PCache node, Map<PNode, Builder<?>> sources)
    {
        super(driver, node, sources);
    }

    private final class Context
            implements BuilderContext
    {
        private final Map<Id, DriverRow> rowsById = new HashMap<>();
        private final Map<Key, List<DriverRow>> rowListsByKey = new HashMap<>();
    }

    @Override
    public BuilderContext buildContext(DriverContextImpl driverContext)
    {
        return new Context();
    }

    @Override
    protected void innerBuild(DriverContextImpl dctx, Key key, Consumer<BuildOp> opConsumer)
    {
        Context ctx = dctx.getBuildContext(this);

        List<DriverRow> cacheRows = ctx.rowListsByKey.get(key);
        if (cacheRows != null) {
            opConsumer.accept(new ResponseBuildOp(this, key, cacheRows));
            return;
        }

        opConsumer.accept(new RequestBuildOp(this, source, key, srows -> {
            ImmutableList.Builder<DriverRow> rows = ImmutableList.builder();

            for (DriverRow row : srows) {
                if (row.getId() != null) {
                    DriverRow cacheRow = ctx.rowsById.get(row.getId());
                    if (cacheRow == null) {
                        cacheRow = new DriverRow(
                                node,
                                dctx.getDriver().getLineagePolicy().build(row),
                                row.getId(),
                                row.getAttributes());
                        ctx.rowsById.put(row.getId(), cacheRow);
                    }
                    rows.add(cacheRow);
                }
                else {
                    rows.add(
                            new DriverRow(
                                    node,
                                    dctx.getDriver().getLineagePolicy().build(row),
                                    row.getId(),
                                    row.getAttributes()));
                }
            }

            List<DriverRow> newCacheRows = rows.build();
            ctx.rowListsByKey.put(key, newCacheRows);
            opConsumer.accept(new ResponseBuildOp(this, key, newCacheRows));
        }));
    }
}
