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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Function;
import com.wrmsr.tokamak.core.driver.DriverImpl;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.build.Builder;
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.RequestBuildOp;
import com.wrmsr.tokamak.core.driver.build.ops.ResponseBuildOp;
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.value.VConstant;
import com.wrmsr.tokamak.core.plan.value.VField;
import com.wrmsr.tokamak.core.plan.value.VFunction;
import com.wrmsr.tokamak.core.plan.value.VNode;
import com.wrmsr.tokamak.core.plan.value.VNodes;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public final class ProjectBuilder
        extends SingleSourceBuilder<PProject>
{
    private final Map<String, String> sourceKeyExtractionMap;

    public ProjectBuilder(DriverImpl driver, PProject node, Map<PNode, Builder<?>> sources)
    {
        super(driver, node, sources);

        ImmutableMap.Builder<String, String> sourceKeyExtractionMap = ImmutableMap.builder();
        node.getProjection().getInputsByOutput().forEach((o, i) -> {
            if (i instanceof VField) {
                sourceKeyExtractionMap.put(o, ((VField) i).getField());
            }
            else if (i instanceof VFunction) {
                VNodes.getIdentityFunctionDirectValueField((VFunction) i).ifPresent(f -> sourceKeyExtractionMap.put(o, f));
            }
        });
        this.sourceKeyExtractionMap = sourceKeyExtractionMap.build();
    }

    private static Object getRowValue(Catalog catalog, Map<String, Object> rowMap, VNode value)
    {
        if (value instanceof VConstant) {
            return ((VConstant) value).getValue();
        }
        else if (value instanceof VField) {
            return rowMap.get(((VField) value).getField());
        }
        else if (value instanceof VFunction) {
            VFunction functionInput = (VFunction) value;
            // FIXME: check lol
            Function function = catalog.getFunctionsByName().get(functionInput.getFunction().getName());
            Executable executable = function.getExecutable();
            // checkState(executable.getType().equals(functionInput.getType()));
            Object[] args = new Object[functionInput.getArgs().size()];
            for (int i = 0; i < args.length; ++i) {
                args[i] = getRowValue(catalog, rowMap, functionInput.getArgs().get(i));
            }
            return executable.invoke(args);
        }
        else {
            throw new IllegalStateException(Objects.toString(value));
        }
    }

    @Override
    protected void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        Key sourceKey;
        sourceKey = Key.of(
                key.stream()
                        .collect(toImmutableMap(
                                e -> checkNotNull(sourceKeyExtractionMap.get(e.getKey())),
                                Map.Entry::getValue)));

        opConsumer.accept(new RequestBuildOp(this, source, sourceKey, srows -> {
            ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
            for (DriverRow row : srows) {
                Map<String, Object> rowMap = row.getMap();
                Object[] attributes = new Object[node.getFields().size()];
                int pos = 0;
                for (Map.Entry<String, VNode> entry : node.getProjection()) {
                    attributes[pos++] = getRowValue(driver.getCatalog(), rowMap, entry.getValue());
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
