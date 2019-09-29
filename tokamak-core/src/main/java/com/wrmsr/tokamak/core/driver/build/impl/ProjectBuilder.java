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
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.plan.node.Node;
import com.wrmsr.tokamak.core.plan.node.ProjectNode;
import com.wrmsr.tokamak.core.plan.node.Projection;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public final class ProjectBuilder
        extends SingleSourceBuilder<ProjectNode>
{
    public ProjectBuilder(DriverImpl driver, ProjectNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        Key sourceKey;
        sourceKey = Key.of(
                key.stream()
                        .collect(toImmutableMap(
                                e -> node.getProjection().getInputFieldsByOutput().get(e.getKey()),
                                Map.Entry::getValue)));

        opConsumer.accept(new RequestBuildOp(source, sourceKey, srows -> {
            ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
            for (DriverRow row : srows) {
                Map<String, Object> rowMap = row.getMap();
                Object[] attributes = new Object[node.getFields().size()];
                int pos = 0;
                for (Map.Entry<String, Projection.Input> entry : node.getProjection()) {
                    Object value;

                    if (entry.getValue() instanceof Projection.FieldInput) {
                        Projection.FieldInput fieldInput = (Projection.FieldInput) entry.getValue();
                        value = rowMap.get(fieldInput.getField());
                    }
                    else if (entry.getValue() instanceof Projection.FunctionInput) {
                        Projection.FunctionInput functionInput = (Projection.FunctionInput) entry.getValue();
                        // FIXME: check lol
                        Function function = context.getDriver().getCatalog().getFunctionsByName()
                                .get(functionInput.getFunction().getName());
                        Executable executable = function.getExecutable();
                        // checkState(executable.getType().equals(functionInput.getType()));
                        Object[] args = new Object[functionInput.getArgs().size()];
                        for (int i = 0; i < args.length; ++i) {
                            args[i] = rowMap.get(functionInput.getArgs().get(i));
                        }
                        value = executable.invoke(args);
                    }
                    else {
                        throw new IllegalStateException(Objects.toString(entry));
                    }

                    attributes[pos++] = value;
                }

                ret.add(
                        new DriverRow(
                                node,
                                context.getDriver().getLineagePolicy().build(row),
                                row.getId(),
                                attributes));
            }

            opConsumer.accept(new ResponseBuildOp(this, key, ret.build()));
        }));
    }
}
