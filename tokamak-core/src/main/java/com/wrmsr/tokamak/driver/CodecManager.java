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
import com.wrmsr.tokamak.codec.row.RowCodec;
import com.wrmsr.tokamak.codec.row.RowCodecs;
import com.wrmsr.tokamak.codec.value.NullableValueCodec;
import com.wrmsr.tokamak.codec.value.TupleValueCodec;
import com.wrmsr.tokamak.codec.value.ValueCodec;
import com.wrmsr.tokamak.codec.value.ValueCodecs;
import com.wrmsr.tokamak.codec.value.VariableLengthValueCodec;
import com.wrmsr.tokamak.driver.state.LinkageMapCodec;
import com.wrmsr.tokamak.driver.state.StateCodec;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.plan.Plan;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public final class CodecManager
{
    private static final int MAX_LINKAGE_SIZE = 128 * 1024 * 1024;
    private static final int MAX_ATTRIBUTES_SIZE = 128 * 1024 * 1024;

    private final Plan plan;

    public CodecManager(Plan plan)
    {
        this.plan = checkNotNull(plan);
    }

    public Plan getPlan()
    {
        return plan;
    }

    private final SupplierLazyValue<Map<StatefulNode, RowCodec>> rowCodecsByStatefulNode = new SupplierLazyValue<>();

    public Map<StatefulNode, RowCodec> getRowCodecsByStatefulNode()
    {
        return rowCodecsByStatefulNode.get(() -> plan.getNodeTypeList(StatefulNode.class).stream()
                .collect(toImmutableMap(identity(), n -> RowCodecs.buildRowCodec(n.getFields()))));
    }

    private final SupplierLazyValue<Map<StatefulNode, ValueCodec<Object[]>>> attributesCodecsByStatefulNode = new SupplierLazyValue<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<StatefulNode, ValueCodec<Object[]>> getAttributesCodecByStatefulNode()
    {
        return attributesCodecsByStatefulNode.get(() -> {
            ImmutableMap.Builder<StatefulNode, ValueCodec<Object[]>> builder = ImmutableMap.builder();

            for (StatefulNode node : plan.getNodeTypeList(StatefulNode.class)) {
                List<ValueCodec> parts = node.getFields().values().stream()
                        .map(t -> {
                            ValueCodec codec = checkNotNull(ValueCodecs.VALUE_CODECS_BY_TYPE.get(t));
                            if (!t.getFixedSize().isPresent()) {
                                codec = new VariableLengthValueCodec(codec, MAX_ATTRIBUTES_SIZE);
                            }
                            codec = new NullableValueCodec(codec);
                            return codec;
                        })
                        .collect(toImmutableList());

                ValueCodec<Object[]> attributesCodec = new TupleValueCodec(parts);
                builder.put(node, attributesCodec);
            }
            return builder.build();
        });
    }

    private final SupplierLazyValue<Map<StatefulNode, StateCodec>> stateCodecsByStatefulNode = new SupplierLazyValue<>();

    public Map<StatefulNode, StateCodec> getStateCodecsByNode()
    {
        return stateCodecsByStatefulNode.get(() -> {
            Map<NodeId, ValueCodec<Object[]>> attributesCodecsByNodeId = getAttributesCodecByStatefulNode().entrySet().stream()
                    .collect(toImmutableMap(e -> e.getKey().getId(), Map.Entry::getValue));

            return plan.getNodeTypeList(StatefulNode.class).stream()
                    .map(n -> new StateCodec(
                            n,
                            getAttributesCodecByStatefulNode().get(n),
                            new LinkageMapCodec(
                                    n,
                                    attributesCodecsByNodeId,
                                    MAX_LINKAGE_SIZE)))
                    .collect(toImmutableMap(StateCodec::getNode, identity()));
        });
    }
}
