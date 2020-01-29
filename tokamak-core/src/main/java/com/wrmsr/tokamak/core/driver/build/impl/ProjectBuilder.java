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
import com.wrmsr.tokamak.core.plan.value.visitor.VNodeVisitor;

import java.util.Map;
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
        node.getProjection().getInputsByOutput().forEach((o, i) -> new VNodeVisitor<Void, Void>()
        {
            @Override
            public Void visitConstant(VConstant node, Void context)
            {
                return null;
            }

            @Override
            public Void visitField(VField field, Void context)
            {
                sourceKeyExtractionMap.put(o, field.getField());
                return null;
            }

            @Override
            public Void visitFunction(VFunction function, Void context)
            {
                VNodes.getIdentityFunctionDirectValueField(function).ifPresent(f -> sourceKeyExtractionMap.put(o, f));
                return null;
            }
        }.process(i, null));
        this.sourceKeyExtractionMap = sourceKeyExtractionMap.build();
    }

    private static Object getRowValue(Catalog catalog, Map<String, Object> rowMap, VNode value)
    {
        return new VNodeVisitor<Object, Void>()
        {
            @Override
            public Object visitConstant(VConstant node, Void context)
            {
                return node.getValue();
            }

            @Override
            public Object visitField(VField node, Void context)
            {
                return rowMap.get(node.getField());
            }

            @Override
            public Object visitFunction(VFunction node, Void context)
            {
                // FIXME: check lol
                Function function = catalog.getFunctionsByName().get(node.getFunction().getName());
                Executable executable = function.getExecutable();
                // checkState(executable.getType().equals(functionInput.getType()));
                Object[] args = new Object[node.getArgs().size()];
                for (int i = 0; i < args.length; ++i) {
                    args[i] = getRowValue(catalog, rowMap, node.getArgs().get(i));
                }
                return executable.invoke(args);
            }
        }.process(value, null);
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
