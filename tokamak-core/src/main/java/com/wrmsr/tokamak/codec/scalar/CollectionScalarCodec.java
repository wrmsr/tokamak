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

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public abstract class CollectionScalarCodec<V>
        implements ScalarCodec<V>
{
    public static final int MAX_BYTE_LENGTH = 255;
    public static final int DEFAULT_MAX_LENGTH = MAX_BYTE_LENGTH;

    protected final int maxLength;
    protected final boolean isByte;

    public CollectionScalarCodec(int maxLength)
    {
        checkArgument(maxLength > 0);
        this.maxLength = maxLength;
        isByte = maxLength <= MAX_BYTE_LENGTH;
    }

    public CollectionScalarCodec()
    {
        this(DEFAULT_MAX_LENGTH);
    }

    protected void encodeSize(int sz, Output output)
    {
        if (isByte) {
            checkArgument(sz < DEFAULT_MAX_LENGTH);
            output.put((byte) sz);
        }
        else {
            output.putLong(sz);
        }
    }

    protected int decodeSize(Input input)
    {
        int sz;
        if (isByte) {
            sz = input.get() & 0xFF;
        }
        else {
            sz = (int) input.getLong();
        }
        return sz;
    }
}
