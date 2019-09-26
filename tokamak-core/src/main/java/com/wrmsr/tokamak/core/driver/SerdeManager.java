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
package com.wrmsr.tokamak.core.driver;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.driver.state.LinkageMapStorageCodec;
import com.wrmsr.tokamak.core.driver.state.StateStorageCodec;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.NodeId;
import com.wrmsr.tokamak.core.plan.node.StateNode;
import com.wrmsr.tokamak.core.serde.row.RowSerde;
import com.wrmsr.tokamak.core.serde.row.RowSerdes;
import com.wrmsr.tokamak.core.serde.value.NullableValueSerde;
import com.wrmsr.tokamak.core.serde.value.TupleValueSerde;
import com.wrmsr.tokamak.core.serde.value.ValueSerde;
import com.wrmsr.tokamak.core.serde.value.ValueSerdes;
import com.wrmsr.tokamak.core.serde.value.VariableLengthValueSerde;
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

    private final SupplierLazyValue<Map<StateNode, RowSerde>> rowSerdesByStateNode = new SupplierLazyValue<>();

    public Map<StateNode, RowSerde> getRowSerdesByStateNode()
    {
        return rowSerdesByStateNode.get(() -> plan.getNodeTypeList(StateNode.class).stream()
                .collect(toImmutableMap(identity(), n -> RowSerdes.buildRowSerde(n.getFields()))));
    }

    private final SupplierLazyValue<Map<StateNode, ValueSerde<Object[]>>> attributesSerdesByStateNode = new SupplierLazyValue<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<StateNode, ValueSerde<Object[]>> getAttributesSerdesByStateNode()
    {
        return attributesSerdesByStateNode.get(() -> {
            ImmutableMap.Builder<StateNode, ValueSerde<Object[]>> builder = ImmutableMap.builder();

            for (StateNode node : plan.getNodeTypeList(StateNode.class)) {
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

    private final SupplierLazyValue<Map<StateNode, StateStorageCodec>> stateStorageCodecByStateNode = new SupplierLazyValue<>();

    public Map<StateNode, StateStorageCodec> getStateStorageCodecsByNode()
    {
        return stateStorageCodecByStateNode.get(() -> {
            Map<NodeId, ValueSerde<Object[]>> attributesSerdeByNodeId = getAttributesSerdesByStateNode().entrySet().stream()
                    .collect(toImmutableMap(e -> e.getKey().getId(), Map.Entry::getValue));

            return plan.getNodeTypeList(StateNode.class).stream()
                    .map(n -> new StateStorageCodec(
                            n,
                            getAttributesSerdesByStateNode().get(n),
                            new LinkageMapStorageCodec(
                                    n,
                                    attributesSerdeByNodeId,
                                    MAX_LINKAGE_SIZE)))
                    .collect(toImmutableMap(StateStorageCodec::getNode, identity()));
        });
    }
}
