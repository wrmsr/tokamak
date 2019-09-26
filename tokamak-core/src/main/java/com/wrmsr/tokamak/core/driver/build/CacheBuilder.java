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
package com.wrmsr.tokamak.core.driver.build;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.CacheNode;
import com.wrmsr.tokamak.core.plan.node.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class CacheBuilder
        extends SingleSourceBuilder<CacheNode>
        implements ContextualBuilder<CacheNode>
{
    public CacheBuilder(DriverImpl driver, CacheNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    private final class Context
            implements BuilderContext
    {
        private final Map<Id, DriverRow> driverRowsById = new HashMap<>();
    }

    @Override
    public BuilderContext buildContext(DriverContextImpl driverContext)
    {
        return new Context();
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl dctx, Key key)
    {
        Context ctx = dctx.getBuildContext(this);

        ImmutableList.Builder<DriverRow> rows = ImmutableList.builder();
        for (DriverRow row : dctx.build(node.getSource(), key)) {
            if (row.getId() != null) {
                DriverRow cacheRow = ctx.driverRowsById.get(row.getId());
                if (cacheRow == null) {
                    cacheRow = new DriverRow(
                            node,
                            dctx.getDriver().getLineagePolicy().build(row),
                            row.getId(),
                            row.getAttributes());
                    ctx.driverRowsById.put(row.getId(), cacheRow);
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
        return rows.build();
    }
}
