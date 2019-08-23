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

import com.wrmsr.tokamak.node.StatefulNode;
import com.wrmsr.tokamak.util.codec.Codec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class StateStorageCodec
    implements Codec<State, StorageState>
{
    private final StatefulNode node;

    public StateStorageCodec(StatefulNode node)
    {
        this.node = checkNotNull(node);
    }

    @Override
    public StorageState encode(State state)
    {
        checkArgument(state.getNode() == node);
        return null;
    }

    @Override
    public State decode(StorageState storageState)
    {
        checkArgument(storageState.getNode() == node);
        return null;
    }
}
