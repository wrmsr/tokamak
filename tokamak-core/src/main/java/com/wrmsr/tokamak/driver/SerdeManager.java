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
package com.wrmsr.tokamak.driver;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.NodeId;
import com.wrmsr.tokamak.serde.row.RowSerde;
import com.wrmsr.tokamak.serde.row.RowSerdes;
import com.wrmsr.tokamak.serde.value.NullableValueSerde;
import com.wrmsr.tokamak.serde.value.TupleValueSerde;
import com.wrmsr.tokamak.serde.value.ValueSerde;
import com.wrmsr.tokamak.serde.value.ValueSerdes;
import com.wrmsr.tokamak.serde.value.VariableLengthValueSerde;
import com.wrmsr.tokamak.driver.state.LinkageMapStorageCodec;
import com.wrmsr.tokamak.driver.state.StateStorageCodec;
import com.wrmsr.tokamak.plan.node.StatefulNode;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public final class SerdeManager
{
    private static final int MAX_LINKAGE_SIZE = 128 * 1024 * 1024;
    private static final int MAX_ATTRIBUTES_SIZE = 128 * 1024 * 1024;

    private final Plan plan;

    public SerdeManager(Plan plan)
    {
        this.plan = checkNotNull(plan);
    }

    public Plan getPlan()
    {
        return plan;
    }

    private final SupplierLazyValue<Map<StatefulNode, RowSerde>> rowSerdesByStatefulNode = new SupplierLazyValue<>();

    public Map<StatefulNode, RowSerde> getRowSerdesByStatefulNode()
    {
        return rowSerdesByStatefulNode.get(() -> plan.getNodeTypeList(StatefulNode.class).stream()
                .collect(toImmutableMap(identity(), n -> RowSerdes.buildRowSerde(n.getFields()))));
    }

    private final SupplierLazyValue<Map<StatefulNode, ValueSerde<Object[]>>> attributesSerdesByStatefulNode = new SupplierLazyValue<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<StatefulNode, ValueSerde<Object[]>> getAttributesSerdesByStatefulNode()
    {
        return attributesSerdesByStatefulNode.get(() -> {
            ImmutableMap.Builder<StatefulNode, ValueSerde<Object[]>> builder = ImmutableMap.builder();

            for (StatefulNode node : plan.getNodeTypeList(StatefulNode.class)) {
                List<ValueSerde> parts = node.getFields().values().stream()
                        .map(t -> {
                            ValueSerde serde = checkNotNull(ValueSerdes.VALUE_SERDES_BY_TYPE.get(t));
                            if (!t.getFixedSize().isPresent()) {
                                serde = new VariableLengthValueSerde(serde, MAX_ATTRIBUTES_SIZE);
                            }
                            serde = new NullableValueSerde(serde);
                            return serde;
                        })
                        .collect(toImmutableList());

                ValueSerde<Object[]> attributesSerde = new TupleValueSerde(parts);
                builder.put(node, attributesSerde);
            }
            return builder.build();
        });
    }

    private final SupplierLazyValue<Map<StatefulNode, StateStorageCodec>> stateStorageCodecByStatefulNode = new SupplierLazyValue<>();

    public Map<StatefulNode, StateStorageCodec> getStateStorageCodecsByNode()
    {
        return stateStorageCodecByStatefulNode.get(() -> {
            Map<NodeId, ValueSerde<Object[]>> attributesSerdeByNodeId = getAttributesSerdesByStatefulNode().entrySet().stream()
                    .collect(toImmutableMap(e -> e.getKey().getId(), Map.Entry::getValue));

            return plan.getNodeTypeList(StatefulNode.class).stream()
                    .map(n -> new StateStorageCodec(
                            n,
                            getAttributesSerdesByStatefulNode().get(n),
                            new LinkageMapStorageCodec(
                                    n,
                                    attributesSerdeByNodeId,
                                    MAX_LINKAGE_SIZE)))
                    .collect(toImmutableMap(StateStorageCodec::getNode, identity()));
        });
    }
}
