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
import com.wrmsr.tokamak.core.catalog.Function;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.driver.context.lineage.LineageEntry;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class FilterBuilder
        extends SingleSourceBuilder<PFilter>
{
    private final Executable executable;

    public FilterBuilder(DriverImpl driver, PFilter node, Map<PNode, Builder<?>> sources)
    {
        super(driver, node, sources);

        Function function = driver.getCatalog().getFunctionsByName()
                .get(node.getFunction().getName());
        executable = function.getExecutable();
    }

    @Override
    protected void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        opConsumer.accept(new RequestBuildOp(this, source, key, srows -> {
            ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
            for (DriverRow row : srows) {
                Object[] args = new Object[node.getArgs().size()];
                for (int i = 0; i < args.length; ++i) {
                    args[i] = row.getMap().get(node.getArgs().get(i));
                }
                Object res = executable.invoke(args);

                Object[] attributes;
                if ((boolean) res) {
                    attributes = row.getAttributes();
                }
                else {
                    attributes = null;
                }

                Set<LineageEntry> lineage;
                switch (node.getLinking()) {
                    case LINKED:
                        lineage = context.getDriver().getLineagePolicy().build(row);
                        break;
                    case UNLINKED:
                        lineage = context.getDriver().getLineagePolicy().build();
                        break;
                    default:
                        throw new IllegalStateException(Objects.toString(node.getLinking()));
                }

                ret.add(
                        new DriverRow(
                                node,
                                lineage,
                                row.getId(),
                                attributes));
            }
            opConsumer.accept(new ResponseBuildOp(this, key, ret.build()));
        }));
    }
}
