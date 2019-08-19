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

package com.wrmsr.tokamak.codec;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class KeyRemappingRowIdCodec
        implements RowIdCodec
{
    private final RowIdCodec child;
    private final Map<String, String> inputsByOutput;
    private final Map<String, String> outputsByInput;

    public KeyRemappingRowIdCodec(RowIdCodec child, Map<String, String> inputsByOutput)
    {
        this.child = checkNotNull(child);
        this.inputsByOutput = ImmutableMap.copyOf(inputsByOutput);
        outputsByInput = inputsByOutput.entrySet().stream().collect(toImmutableMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Override
    public Map<String, Object> decode(byte[] data)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] encode(Map<String, Object> data)
    {
        ImmutableMap.Builder<String, Object> childData = ImmutableMap.builder();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            String remappedKey = inputsByOutput.get(e.getKey());
            childData.put(remappedKey != null ? remappedKey : e.getKey(), e.getValue());
        }
        return child.encode(childData.build());
    }
}
