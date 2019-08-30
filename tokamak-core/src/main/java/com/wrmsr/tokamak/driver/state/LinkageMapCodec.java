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
import com.wrmsr.tokamak.codec.ByteArrayInput;
import com.wrmsr.tokamak.codec.ByteArrayOutput;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;
import com.wrmsr.tokamak.codec.value.ValueCodec;
import com.wrmsr.tokamak.codec.value.ValueCodecs;
import com.wrmsr.tokamak.codec.value.VariableLengthValueCodec;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.codec.Codec;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class LinkageMapCodec
        implements Codec<Map<NodeId, Linkage.Links>, byte[]>
{
    private static final ValueCodec<Number> LONG_CODEC = ValueCodecs.LONG_VALUE_CODEC;
    private static final ValueCodec<byte[]> VAR_BYTES_CODEC = new VariableLengthValueCodec<>(ValueCodecs.BYTES_VALUE_CODEC);

    private final StatefulNode node;
    private final Map<NodeId, ValueCodec<Object[]>> attributesCodecsByNodeId;

    public LinkageMapCodec(StatefulNode node, Map<NodeId, ValueCodec<Object[]>> attributesCodecsByNodeId)
    {
        this.node = checkNotNull(node);
        this.attributesCodecsByNodeId = ImmutableMap.copyOf(attributesCodecsByNodeId);
        Set<NodeId> sourceNodeIds = node.getSources().stream().map(Node::getId).collect(toImmutableSet());
        checkArgument(sourceNodeIds.equals(attributesCodecsByNodeId.keySet()));
    }

    public LinkageMapCodec(StatefulNode node)
    {
        this(node, ImmutableMap.of());
    }

    private void encodeIdLinks(NodeId nodeId, Linkage.IdLinks idLinks, Output output)
    {
        LONG_CODEC.encode(idLinks.getIds().size(), output);
        for (Id id : idLinks.getIds()) {
            VAR_BYTES_CODEC.encode(id.getValue(), output);
        }
    }

    private Linkage.IdLinks decodeIdLinks(NodeId nodeId, Input input)
    {
        int sz = (int) LONG_CODEC.decode(input);
        ImmutableSet.Builder<Id> builder = ImmutableSet.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.add(Id.of(VAR_BYTES_CODEC.decode(input)));
        }
        return new Linkage.IdLinks(builder.build());
    }

    private void encodeDenormalizedLinks(NodeId nodeId, Linkage.DenormalizedLinks denormalizedLinks, Output output)
    {
        ValueCodec<Object[]> attributesCodec = checkNotNull(attributesCodecsByNodeId.get(nodeId));
        LONG_CODEC.encode(denormalizedLinks.getAttributesById().size(), output);
        for (Map.Entry<Id, Object[]> entry : denormalizedLinks.getAttributesById().entrySet()) {
            VAR_BYTES_CODEC.encode(entry.getKey().getValue(), output);
            attributesCodec.encode(entry.getValue(), output);
        }
    }

    private Linkage.DenormalizedLinks decodeDenormalizedLinks(NodeId nodeId, Input input)
    {
        ValueCodec<Object[]> attributesCodec = checkNotNull(attributesCodecsByNodeId.get(nodeId));
        int sz = (int) LONG_CODEC.decode(input);
        ImmutableMap.Builder<Id, Object[]> builder = ImmutableMap.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.put(
                    Id.of(VAR_BYTES_CODEC.decode(input)),
                    attributesCodec.decode(input));
        }
        return new Linkage.DenormalizedLinks(builder.build());
    }

    @Override
    public byte[] encode(Map<NodeId, Linkage.Links> linksMap)
    {
        ByteArrayOutput output = new ByteArrayOutput();
        LONG_CODEC.encode(linksMap.size(), output);
        for (Map.Entry<NodeId, Linkage.Links> entry : linksMap.entrySet()) {
            NodeId nodeId = entry.getKey();
            Linkage.Links links = entry.getValue();
            LONG_CODEC.encode(nodeId.getValue(), output);
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
        int sz = (int) LONG_CODEC.decode(input);
        ImmutableMap.Builder<NodeId, Linkage.Links> builder = ImmutableMap.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            NodeId nodeId = NodeId.of((int) LONG_CODEC.decode(input));
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
