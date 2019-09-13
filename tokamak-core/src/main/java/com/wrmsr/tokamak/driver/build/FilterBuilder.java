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
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.catalog.Function;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.func.Executable;
import com.wrmsr.tokamak.node.FilterNode;
import com.wrmsr.tokamak.node.Node;

import java.util.Collection;
import java.util.Map;

public final class FilterBuilder
        extends SingleSourceBuilder<FilterNode>
{
    public FilterBuilder(DriverImpl driver, FilterNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected Collection<DriverRow> innerBuild(DriverContextImpl context, Key key)
    {
        ImmutableList.Builder<DriverRow> ret = ImmutableList.builder();
        Function function = context.getDriver().getCatalog().getFunctionsByName()
                .get(node.getPredicate().getName());
        Executable executable = function.getExecutable();
        for (DriverRow row : context.build(source, key)) {
            Object[] attributes;
            if (node.getPredicate().test(row)) {
                attributes = row.getAttributes();
            }
            else {
                attributes = null;
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
