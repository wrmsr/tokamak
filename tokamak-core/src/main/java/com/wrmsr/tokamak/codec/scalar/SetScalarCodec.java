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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class SetScalarCodec<V>
        implements ScalarCodec<Set<V>>
{
    public static final int MAX_BYTE_LENGTH = 255;
    public static final int DEFAULT_MAX_LENGTH = MAX_BYTE_LENGTH;

    private final ScalarCodec<V> child;
    private final int maxLength;
    private final boolean isByte;

    public SetScalarCodec(ScalarCodec<V> child, int maxLength)
    {
        checkArgument(maxLength > 0);
        this.child = checkNotNull(child);
        this.maxLength = maxLength;
        isByte = maxLength <= MAX_BYTE_LENGTH;
    }

    public SetScalarCodec(ScalarCodec<V> child)
    {
        this(child, DEFAULT_MAX_LENGTH);
    }

    @Override
    public void encode(Set<V> value, Output output)
    {
        if (isByte) {
            checkArgument(value.size() < DEFAULT_MAX_LENGTH);
            output.put((byte) value.size());
        }
        else {
            output.putLong(value.size());
        }
        for (V item : value) {
            child.encode(item, output);
        }
    }

    @Override
    public Set<V> decode(Input input)
    {
        int sz;
        if (isByte) {
            sz = input.get() & 0xFF;
        }
        else {
            sz = (int) input.getLong();
        }
        ImmutableSet.Builder<V> builder = ImmutableSet.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.add(child.decode(input));
        }
        return builder.build();
    }
}
