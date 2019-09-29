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
import com.wrmsr.tokamak.core.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.core.plan.node.ListAggregateNode;
import com.wrmsr.tokamak.core.plan.node.Node;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class ListAggregateBuilder
        extends SingleSourceBuilder<ListAggregateNode>
{
    public ListAggregateBuilder(DriverImpl driver, ListAggregateNode node, Map<Node, Builder> sources)
    {
        super(driver, node, sources);
    }

    @Override
    protected void innerBuild(DriverContextImpl context, Key key, Consumer<BuildOp> opConsumer)
    {
        // RowCodec idCodec = context.getDriver().getCodecManager().getRowIdCodec(node);
        Key childKey;
        Map.Entry<String, Object> fieldKeyEntry = checkSingle(key);
        checkArgument(fieldKeyEntry.getKey().equals(node.getGroupField()));
        childKey = Key.of(node.getGroupField(), fieldKeyEntry.getValue());

        // Collection<DriverRow> rows = context.build(node.getSource(), key);
        // if (rows.size() == 1 && checkSingle(rows).isNull()) {
        //
        // }
        //
        // Map<Object, List<DriverRow>> groups = new LinkedHashMap<>();
        // for (DriverRow row : rows) {
        //     if (row.isNull()) {
        //
        //     }
        //     // Object group =
        //     //         groups.computeIfAbsent(ro)
        // }

        throw new IllegalStateException();
    }
}
