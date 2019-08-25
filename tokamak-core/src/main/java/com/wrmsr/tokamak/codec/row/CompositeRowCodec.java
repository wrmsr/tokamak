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
package com.wrmsr.tokamak.codec.row;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class CompositeRowCodec
        implements RowCodec, StreamableIterable<RowCodec>
{
    private final List<RowCodec> children;

    public CompositeRowCodec(List<RowCodec> children)
    {
        this.children = ImmutableList.copyOf(children);
    }

    public List<RowCodec> getChildren()
    {
        return children;
    }

    @Override
    public Iterator<RowCodec> iterator()
    {
        return children.iterator();
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        for (RowCodec child : children) {
            child.encode(row, output);
        }
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        for (RowCodec child : children) {
            child.decode(sink, input);
        }
    }
}
