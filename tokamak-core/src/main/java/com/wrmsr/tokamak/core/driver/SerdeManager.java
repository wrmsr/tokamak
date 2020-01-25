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
import com.wrmsr.tokamak.core.plan.node.PNodeId;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Serdes;
import com.wrmsr.tokamak.core.serde.impl.NullableSerde;
import com.wrmsr.tokamak.core.serde.impl.TupleSerde;
import com.wrmsr.tokamak.core.serde.impl.VariableLengthSerde;
import com.wrmsr.tokamak.core.type.TypeAnnotations;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@SuppressWarnings({"rawtypes"})
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

    private final SupplierLazyValue<Map<PState, Serde<Object[]>>> attributesSerdesByStateNode = new SupplierLazyValue<>();

    @SuppressWarnings({"unchecked"})
    public Map<PState, Serde<Object[]>> getAttributesSerdesByStateNode()
    {
        return attributesSerdesByStateNode.get(() -> {
            ImmutableMap.Builder<PState, Serde<Object[]>> builder = ImmutableMap.builder();

            for (PState node : plan.getNodeTypeList(PState.class)) {
                List<Serde> parts = node.getFields().getTypesByName().values().stream()
                        .map(t -> {
                            Serde serde = checkNotNull(Serdes.VALUE_SERDES_BY_TYPE.get(TypeAnnotations.strip(t)));
                            if (!t.getFixedSize().isPresent()) {
                                serde = new VariableLengthSerde(serde, MAX_ATTRIBUTES_SIZE);
                            }
                            serde = new NullableSerde(serde);
                            return serde;
                        })
                        .collect(toImmutableList());

                Serde<Object[]> attributesSerde = new TupleSerde(parts);
                builder.put(node, attributesSerde);
            }
            return builder.build();
        });
    }

    private final SupplierLazyValue<Map<PState, StateStorageCodec>> stateStorageCodecByStateNode = new SupplierLazyValue<>();

    public Map<PState, StateStorageCodec> getStateStorageCodecsByNode()
    {
        return stateStorageCodecByStateNode.get(() -> {
            Map<PNodeId, Serde<Object[]>> attributesSerdeByNodeId = getAttributesSerdesByStateNode().entrySet().stream()
                    .collect(toImmutableMap(e -> e.getKey().getId(), Map.Entry::getValue));

            return plan.getNodeTypeList(PState.class).stream()
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
