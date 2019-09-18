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
package com.wrmsr.tokamak.serde.row;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.serde.Input;
import com.wrmsr.tokamak.serde.Output;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class CompositeRowSerde
        implements RowSerde, StreamableIterable<RowSerde>
{
    private final List<RowSerde> children;

    public CompositeRowSerde(List<RowSerde> children)
    {
        this.children = ImmutableList.copyOf(children);
    }

    public List<RowSerde> getChildren()
    {
        return children;
    }

    @Override
    public Iterator<RowSerde> iterator()
    {
        return children.iterator();
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        for (RowSerde child : children) {
            child.encode(row, output);
        }
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        for (RowSerde child : children) {
            child.decode(sink, input);
        }
    }
}
