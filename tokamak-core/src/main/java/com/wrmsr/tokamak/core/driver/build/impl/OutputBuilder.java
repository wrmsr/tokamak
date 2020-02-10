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

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.POutput;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class OutputBuilder
        extends SingleSourceBuilder<POutput>
{
    public OutputBuilder(DriverImpl driver, POutput node, Map<PNode, Builder<?>> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected void innerBuild(DriverContextImpl dctx, Key key, Consumer<BuildOp> opConsumer)
    {
        opConsumer.accept(new RequestBuildOp(this, source, key, map -> {
            List<DriverRow> rows = checkSingle(checkSingle(map.values()).values()).stream()
                    .map(srow -> new DriverRow(
                            node,
                            dctx.getDriver().getLineagePolicy().build(srow),
                            srow.getId(),
                            srow.getAttributes()))
                    .collect(toImmutableList());
            opConsumer.accept(new ResponseBuildOp(this, key, rows));
        }));
    }
}
