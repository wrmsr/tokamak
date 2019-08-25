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

import com.wrmsr.tokamak.codec.scalar.ListScalarCodec;
import com.wrmsr.tokamak.codec.scalar.ScalarCodecs;
import com.wrmsr.tokamak.codec.scalar.VariableLengthScalarCodec;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.codec.Codec;

import static com.google.common.base.Preconditions.checkNotNull;

public final class LinkageCodec
        implements Codec<Linkage, byte[]>
{
    private static final ListScalarCodec<byte[]> BYTES_LIST_CODEC =
            new ListScalarCodec<>(
                    new VariableLengthScalarCodec<>(
                            ScalarCodecs.BYTES_SCALAR_CODEC));

    private final StatefulNode node;

    public LinkageCodec(StatefulNode node)
    {
        this.node = checkNotNull(node);
    }

    // public byte[] encode(Linkage.IdLinks data)
    // {
    //     List<byte[]> bytesList = data.getIds().stream()
    //             .map(Id::getValue)
    //             .collect(toImmutableList());
    //     return BYTES_LIST_CODEC.encodeBytes(bytesList);
    // }

    // public Linkage.IdLinks decode(byte[] data)
    // {
    //     List<byte[]> bytesList = BYTES_LIST_CODEC.decodeBytes(data);
    //     return new Linkage.IdLinks(
    //             bytesList.stream()
    //                     .map(Id::of)
    //                     .collect(toImmutableSet()));
    // }

    @Override
    public byte[] encode(Linkage data)
    {
        return new byte[0];
    }

    @Override
    public Linkage decode(byte[] data)
    {
        return null;
    }
}
