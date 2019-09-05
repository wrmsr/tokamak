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

import com.wrmsr.tokamak.codec.value.PrefixedValueCodec;
import com.wrmsr.tokamak.codec.value.ValueCodec;
import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.codec.Codec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class StateCodec
        implements Codec<State, StorageState>
{
    private static final byte ATTRIBUTES_PREFIX = 0x41;

    private final StatefulNode node;
    private final ValueCodec<Object[]> attributesCodec;
    private final LinkageMapCodec linkageMapCodec;

    private final ValueCodec<Object[]> prefixedAttributesCodec;

    public StateCodec(
            StatefulNode node,
            ValueCodec<Object[]> attributesCodec,
            LinkageMapCodec linkageMapCodec)
    {
        this.node = checkNotNull(node);
        this.attributesCodec = checkNotNull(attributesCodec);
        this.linkageMapCodec = checkNotNull(linkageMapCodec);

        prefixedAttributesCodec = new PrefixedValueCodec<>(new byte[] {ATTRIBUTES_PREFIX}, this.attributesCodec);
    }

    public StatefulNode getNode()
    {
        return node;
    }

    @Override
    public StorageState encode(State state)
    {
        checkNotNull(state);
        checkArgument(state.getNode() == node);

        byte[] attributes = null;
        if (state.getAttributes() != null) {
            attributes = prefixedAttributesCodec.encodeBytes(state.getAttributes());
        }

        byte[] input = null;
        byte[] output = null;
        if (state.getLinkage() != null) {
            input = linkageMapCodec.encode(state.getLinkage().getInput());
            output = linkageMapCodec.encode(state.getLinkage().getOutput());
        }

        return new StorageState(
                node,
                state.getId(),
                StorageState.Mode.EXCLUSIVE,
                state.getVersion(),
                0.0f,
                0.0f,
                attributes,
                input,
                output);
    }

    @Override
    public State decode(StorageState storageState)
    {
        checkNotNull(storageState);
        checkArgument(storageState.getNode() == node);

        Object[] attributes = null;
        if (storageState.getAttributes() != null) {
            attributes = prefixedAttributesCodec.decodeBytes(storageState.getAttributes());
        }

        Linkage linkage = null;
        if (storageState.getInput() != null || storageState.getOutput() != null) {
            checkArgument(storageState.getInput() != null && storageState.getOutput() != null);
            linkage = new Linkage(
                    linkageMapCodec.decode(storageState.getInput()),
                    linkageMapCodec.decode(storageState.getOutput()));
        }

        return State.newFromStorage(
                node,
                storageState.getId(),
                storageState.getMode(),
                storageState.getVersion(),
                attributes,
                linkage);
    }
}
