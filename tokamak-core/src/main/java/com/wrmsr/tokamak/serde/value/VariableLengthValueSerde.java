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
package com.wrmsr.tokamak.serde.value;

import com.wrmsr.tokamak.serde.Input;
import com.wrmsr.tokamak.serde.Output;
import com.wrmsr.tokamak.serde.Width;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Immutable
public final class VariableLengthValueSerde<V>
        implements ValueSerde<V>
{
    public static final int MAX_BYTE_SIZE = 255;
    public static final int DEFAULT_MAX_SIZE = MAX_BYTE_SIZE;

    private final ValueSerde<V> child;
    private final int size;
    private final boolean byteSized;

    public VariableLengthValueSerde(ValueSerde<V> child, int size)
    {
        checkArgument(size > 0);
        this.child = checkNotNull(child);
        this.size = size;
        byteSized = size <= MAX_BYTE_SIZE;
    }

    public VariableLengthValueSerde(ValueSerde<V> child)
    {
        this(child, DEFAULT_MAX_SIZE);
    }

    @Override
    public Width getWidth()
    {
        return child.getWidth().map(w -> w + (byteSized ? 1 : 8));
    }

    @Override
    public void write(V value, Output output)
    {
        int startPos = output.tell();
        if (byteSized) {
            output.put((byte) 0);
        }
        else {
            output.putLong(0);
        }
        child.write(value, output);
        int endPos = output.tell();
        int sz = endPos - startPos - (byteSized ? 1 : 8);
        checkState(sz < size);
        if (byteSized) {
            output.putAt(startPos, (byte) sz);
        }
        else {
            output.putLongAt(startPos, sz);
        }
    }

    @Override
    public V read(Input input)
    {
        int sz;
        if (byteSized) {
            sz = input.get() & 0xFF;
        }
        else {
            sz = (int) input.getLong();
        }
        return child.read(input.nest(sz));
    }
}
