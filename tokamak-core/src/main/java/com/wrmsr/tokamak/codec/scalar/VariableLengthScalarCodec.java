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
package com.wrmsr.tokamak.codec.scalar;

import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class VariableLengthScalarCodec<V>
        implements ScalarCodec<V>
{
    public static final int MAX_BYTE_LENGTH = 255;
    public static final int DEFAULT_MAX_LENGTH = MAX_BYTE_LENGTH;

    private final ScalarCodec<V> child;
    private final int maxLength;
    private final boolean isByte;

    public VariableLengthScalarCodec(ScalarCodec<V> child, int maxLength)
    {
        checkArgument(maxLength > 0);
        this.child = checkNotNull(child);
        this.maxLength = maxLength;
        isByte = maxLength <= MAX_BYTE_LENGTH;
    }

    public VariableLengthScalarCodec(ScalarCodec<V> child)
    {
        this(child, DEFAULT_MAX_LENGTH);
    }

    @Override
    public void encode(V value, Output output)
    {
       int pos = output.tell();
        if (isByte) {
            output.put((byte)0);
        }
        else{
            output.putLong(0);
        }
        child.encode(value, output);
        int sz = output.tell() - pos;
        checkState(sz < maxLength);
        if (isByte) {
            output.putAt(pos, (byte) sz);
        }
        else {
            output.putLongAt(pos, sz);
        }
    }

    @Override
    public V decode(Input input)
    {
        int sz;
        if (isByte) {
            sz = input.get() & 0xFF;
        }
        else {
            sz = (int) input.getLong();
        }
        return child.decode(input.nest(sz));
    }
}
