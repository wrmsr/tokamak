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

import com.wrmsr.tokamak.codec.row.RowCodec;
import com.wrmsr.tokamak.codec.row.RowCodecs;
import com.wrmsr.tokamak.codec.value.NullableValueCodec;
import com.wrmsr.tokamak.codec.value.TupleValueCodec;
import com.wrmsr.tokamak.codec.value.ValueCodec;
import com.wrmsr.tokamak.codec.value.ValueCodecs;
import com.wrmsr.tokamak.codec.value.VariableLengthValueCodec;
import com.wrmsr.tokamak.node.StatefulNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class CodecManager
{
    private final Map<StatefulNode, RowCodec> rowCodecsByStatefulNode = new HashMap<>();

    public RowCodec getRowCodec(StatefulNode node)
    {
        RowCodec rowCodec = rowCodecsByStatefulNode.get(node);
        if (rowCodec != null) {
            return rowCodec;
        }

        rowCodec = RowCodecs.buildRowCodec(node.getFields());
        rowCodecsByStatefulNode.put(node, rowCodec);
        return rowCodec;
    }

    private final Map<StatefulNode, ValueCodec<Object[]>> attributesCodecsByStatefulNode = new HashMap<>();

    public ValueCodec<Object[]> getAttributesCodec(StatefulNode node)
    {
        ValueCodec<Object[]> attributesCodec = attributesCodecsByStatefulNode.get(node);
        if (attributesCodec != null) {
            return attributesCodec;
        }

        List<ValueCodec> parts = node.getFields().values().stream()
                .map(t -> {
                    ValueCodec codec = checkNotNull(ValueCodecs.VALUE_CODECS_BY_TYPE.get(t));
                    if (!t.getFixedSize().isPresent()) {
                        codec = new VariableLengthValueCodec(codec);
                    }
                    codec = new NullableValueCodec(codec);
                    return codec;
                })
                .collect(toImmutableList());
        attributesCodec = new TupleValueCodec(parts);

        attributesCodecsByStatefulNode.put(node, attributesCodec);
        return attributesCodec;
    }
}
