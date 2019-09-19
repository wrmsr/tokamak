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

import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.IdKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.driver.DriverImpl;
import com.wrmsr.tokamak.driver.DriverRow;
import com.wrmsr.tokamak.driver.context.DriverContextImpl;
import com.wrmsr.tokamak.plan.node.ListAggregateNode;
import com.wrmsr.tokamak.plan.node.Node;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    protected Collection<DriverRow> innerBuild(DriverContextImpl context, Key key)
    {
        // RowCodec idCodec = context.getDriver().getCodecManager().getRowIdCodec(node);
        Key childKey;
        if (key instanceof IdKey) {
            byte[] buf = ((IdKey) key).getId().getValue();
            // childKey = Key.of(node.getGroupField(), idCodec.decodeSingle(node.getGroupField(), new ByteArrayInput(buf)));
        }
        else if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Map.Entry<String, Object> fieldKeyEntry = checkSingle(fieldKey);
            checkArgument(fieldKeyEntry.getKey().equals(node.getGroupField()));
            childKey = Key.of(node.getGroupField(), fieldKeyEntry.getValue());
        }
        else if (key instanceof AllKey) {
            childKey = key;
        }
        else {
            throw new IllegalArgumentException(Objects.toString(key));
        }

        Collection<DriverRow> rows = context.build(node.getSource(), key);
        if (rows.size() == 1 && checkSingle(rows).isNull()) {

        }

        Map<Object, List<DriverRow>> groups = new LinkedHashMap<>();
        for (DriverRow row : rows) {
            if (row.isNull()) {

            }
            // Object group =
            //         groups.computeIfAbsent(ro)
        }

        throw new IllegalStateException();
    }
}
