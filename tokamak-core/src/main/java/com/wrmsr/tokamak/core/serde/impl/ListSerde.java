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
package com.wrmsr.tokamak.core.serde.impl;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.serde.Input;
import com.wrmsr.tokamak.core.serde.Output;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Width;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ListSerde<V>
        extends CollectionSerde<List<V>>
{
    private final Serde<V> child;

    public ListSerde(Serde<V> child, int size, boolean fixed)
    {
        super(size, fixed);
        checkArgument(size > 0);
        this.child = checkNotNull(child);
    }

    public ListSerde(Serde<V> child)
    {
        this(child, DEFAULT_MAX_SIZE, false);
    }

    @Override
    public Width getEntryWidth()
    {
        return child.getWidth();
    }

    @Override
    public void write(List<V> value, Output output)
    {
        encodeSize(value.size(), output);
        for (V item : value) {
            child.write(item, output);
        }
    }

    @Override
    public List<V> read(Input input)
    {
        int sz = decodeSize(input);
        ImmutableList.Builder<V> builder = ImmutableList.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.add(child.read(input));
        }
        return builder.build();
    }
}
