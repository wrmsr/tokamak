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
package com.wrmsr.tokamak.driver.build;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.catalog.Function;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.exec.Executable;
import com.wrmsr.tokamak.exec.RowExecutable;
import com.wrmsr.tokamak.exec.RowMapExecutable;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.ProjectNode;
import com.wrmsr.tokamak.node.Projection;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public final class ProjectBuilder
        extends SingleSourceBuilder<ProjectNode>
{
    public ProjectBuilder(DriverImpl driver, ProjectNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl context, Key key)
    {
        Key sourceKey;
        if (key instanceof IdKey || key instanceof AllKey) {
            sourceKey = key;
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            sourceKey = Key.of(
                    fieldKey.stream()
                            .collect(toImmutableMap(
                                    e -> node.getProjection().getInputFieldsByOutput().get(e.getKey()),
                                    Map.Entry::getValue)));
        }
        else {
            throw new IllegalArgumentException(Objects.toString(key));
        }

        ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
        for (DriverRow row : context.build(source, sourceKey)) {
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
                    if (executable instanceof RowExecutable) {
                        value = ((RowExecutable) executable).invoke(row);
                    }
                    else if (executable instanceof RowMapExecutable) {
                        value = ((RowMapExecutable) executable).invoke(rowMap);
                    }
                    else {
                        throw new IllegalStateException(Objects.toString(executable));
                    }
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

        return ret.build();
    }
}
