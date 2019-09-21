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

import com.wrmsr.tokamak.serde.value.PrefixedValueSerde;
import com.wrmsr.tokamak.serde.value.ValueSerde;
import com.wrmsr.tokamak.plan.node.StatefulNode;
import com.wrmsr.tokamak.util.codec.Codec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class StateStorageCodec
        implements Codec<State, StorageState>
{
    private static final byte ATTRIBUTES_PREFIX = 0x41;

    private final StatefulNode node;
    private final ValueSerde<Object[]> attributesSerde;
    private final LinkageMapStorageCodec linkageMapStorageCodec;

    private final ValueSerde<Object[]> prefixedAttributesSerde;

    public StateStorageCodec(
            StatefulNode node,
            ValueSerde<Object[]> attributesSerde,
            LinkageMapStorageCodec linkageMapStorageCodec)
    {
        this.node = checkNotNull(node);
        this.attributesSerde = checkNotNull(attributesSerde);
        this.linkageMapStorageCodec = checkNotNull(linkageMapStorageCodec);

        prefixedAttributesSerde = new PrefixedValueSerde<>(new byte[] {ATTRIBUTES_PREFIX}, this.attributesSerde);
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
            attributes = prefixedAttributesSerde.writeBytes(state.getAttributes());
        }

        byte[] input = null;
        byte[] output = null;
        if (state.getLinkage() != null) {
            input = linkageMapStorageCodec.encode(state.getLinkage().getInput());
            output = linkageMapStorageCodec.encode(state.getLinkage().getOutput());
        }

        return new StorageState(
                node,
                state.getId(),
                StorageState.Mode.EXCLUSIVE,
                state.getVersion(),
                0.0f,
                0.0f,
                attributes,
                state.getAttributesVersion(),
                input,
                output,
                state.getLinkageVersion());
    }

    @Override
    public State decode(StorageState storageState)
    {
        checkNotNull(storageState);
        checkArgument(storageState.getNode() == node);

        Object[] attributes = null;
        if (storageState.getAttributes() != null) {
            attributes = prefixedAttributesSerde.readBytes(storageState.getAttributes());
        }

        Linkage linkage = null;
        if (storageState.getInput() != null || storageState.getOutput() != null) {
            checkArgument(storageState.getInput() != null && storageState.getOutput() != null);
            linkage = new Linkage(
                    linkageMapStorageCodec.decode(storageState.getInput()),
                    linkageMapStorageCodec.decode(storageState.getOutput()));
        }

        return State.newFromStorage(
                node,
                storageState.getId(),
                storageState.getMode(),
                storageState.getVersion(),
                attributes,
                storageState.getAttributesVersion(),
                linkage,
                storageState.getLinkageVersion());
    }
}
