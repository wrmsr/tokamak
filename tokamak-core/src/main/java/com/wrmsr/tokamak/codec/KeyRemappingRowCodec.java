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

public final class KeyRemappingRowCodec
        implements RowCodec
{
    private final RowCodec child;
    private final Map<String, String> inputsByOutput;
    private final Map<String, String> outputsByInput;

    public KeyRemappingRowCodec(RowCodec child, Map<String, String> inputsByOutput)
    {
        this.child = checkNotNull(child);
        this.inputsByOutput = ImmutableMap.copyOf(inputsByOutput);
        outputsByInput = inputsByOutput.entrySet().stream().collect(toImmutableMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        ImmutableMap.Builder<String, Object> childData = ImmutableMap.builder();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            String remappedKey = inputsByOutput.get(e.getKey());
            childData.put(remappedKey != null ? remappedKey : e.getKey(), e.getValue());
        }
        child.encode(childData.build(), output);
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        child.decode((k, v) -> sink.put(inputsByOutput.getOrDefault(k, k), v), input);
    }
}