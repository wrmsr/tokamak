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
package com.wrmsr.tokamak.driver.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.NodeId;
import com.wrmsr.tokamak.serde.ByteArrayInput;
import com.wrmsr.tokamak.serde.ByteArrayOutput;
import com.wrmsr.tokamak.serde.Input;
import com.wrmsr.tokamak.serde.Output;
import com.wrmsr.tokamak.serde.value.ValueSerde;
import com.wrmsr.tokamak.serde.value.ValueSerdes;
import com.wrmsr.tokamak.serde.value.VariableLengthValueSerde;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.codec.Codec;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class LinkageMapStorageCodec
        implements Codec<Map<NodeId, Linkage.Links>, byte[]>
{
    private static final byte PREFIX = 0x42;

    private static final ValueSerde<Number> LONG_SERDE = ValueSerdes.LONG_VALUE_SERDE;

    private final StatefulNode node;
    private final Map<NodeId, ValueSerde<Object[]>> attributesSerdesByNodeId;
    private final int size;

    private final ValueSerde<byte[]> varBytesSerde;

    public LinkageMapStorageCodec(
            StatefulNode node,
            Map<NodeId, ValueSerde<Object[]>> attributesSerdesByNodeId,
            int size)
    {
        checkArgument(size >= 0);
        this.node = checkNotNull(node);
        this.attributesSerdesByNodeId = ImmutableMap.copyOf(attributesSerdesByNodeId);
        this.size = size;
        Set<NodeId> sourceNodeIds = node.getSources().stream().map(Node::getId).collect(toImmutableSet());
        sourceNodeIds.forEach(sni -> checkArgument(attributesSerdesByNodeId.containsKey(sni)));

        varBytesSerde = new VariableLengthValueSerde<>(ValueSerdes.BYTES_VALUE_SERDE);
    }

    public StatefulNode getNode()
    {
        return node;
    }

    private void encodeIdLinks(NodeId nodeId, Linkage.IdLinks idLinks, Output output)
    {
        LONG_SERDE.write(idLinks.getIds().size(), output);
        for (Id id : idLinks.getIds()) {
            varBytesSerde.write(id.getValue(), output);
        }
    }

    private Linkage.IdLinks decodeIdLinks(NodeId nodeId, Input input)
    {
        int sz = (int) LONG_SERDE.read(input);
        ImmutableSet.Builder<Id> builder = ImmutableSet.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.add(Id.of(varBytesSerde.read(input)));
        }
        return new Linkage.IdLinks(builder.build());
    }

    private void encodeDenormalizedLinks(NodeId nodeId, Linkage.DenormalizedLinks denormalizedLinks, Output output)
    {
        ValueSerde<Object[]> attributesSerde = checkNotNull(attributesSerdesByNodeId.get(nodeId));
        LONG_SERDE.write(denormalizedLinks.getAttributesById().size(), output);
        for (Map.Entry<Id, Object[]> entry : denormalizedLinks.getAttributesById().entrySet()) {
            varBytesSerde.write(entry.getKey().getValue(), output);
            attributesSerde.write(entry.getValue(), output);
        }
    }

    private Linkage.DenormalizedLinks decodeDenormalizedLinks(NodeId nodeId, Input input)
    {
        ValueSerde<Object[]> attributesSerde = checkNotNull(attributesSerdesByNodeId.get(nodeId));
        int sz = (int) LONG_SERDE.read(input);
        ImmutableMap.Builder<Id, Object[]> builder = ImmutableMap.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.put(
                    Id.of(varBytesSerde.read(input)),
                    attributesSerde.read(input));
        }
        return new Linkage.DenormalizedLinks(builder.build());
    }

    @Override
    public byte[] encode(Map<NodeId, Linkage.Links> linksMap)
    {
        ByteArrayOutput output = new ByteArrayOutput();
        output.put(PREFIX);
        LONG_SERDE.write(linksMap.size(), output);
        for (Map.Entry<NodeId, Linkage.Links> entry : linksMap.entrySet()) {
            NodeId nodeId = entry.getKey();
            Linkage.Links links = entry.getValue();
            LONG_SERDE.write(nodeId.getValue(), output);
            if (links instanceof Linkage.IdLinks) {
                output.put((byte) 0);
                encodeIdLinks(nodeId, (Linkage.IdLinks) links, output);
            }
            else if (links instanceof Linkage.DenormalizedLinks) {
                output.put((byte) 1);
                encodeDenormalizedLinks(nodeId, (Linkage.DenormalizedLinks) links, output);
            }
            else {
                throw new IllegalArgumentException(Objects.toString(links));
            }
        }
        return output.toByteArray();
    }

    @Override
    public Map<NodeId, Linkage.Links> decode(byte[] data)
    {
        ByteArrayInput input = new ByteArrayInput(data);
        byte prefix = input.get();
        checkState(prefix == PREFIX);
        int sz = (int) LONG_SERDE.read(input);
        ImmutableMap.Builder<NodeId, Linkage.Links> builder = ImmutableMap.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            NodeId nodeId = NodeId.of((int) LONG_SERDE.read(input));
            byte tag = input.get();
            Linkage.Links links;
            if (tag == (byte) 0) {
                links = decodeIdLinks(nodeId, input);
            }
            else if (tag == (byte) 1) {
                links = decodeDenormalizedLinks(nodeId, input);
            }
            else {
                throw new IllegalStateException(Byte.toString(tag));
            }
            builder.put(nodeId, links);
        }
        return builder.build();
    }
}
