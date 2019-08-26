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

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class SetScalarCodec<V>
        extends CollectionScalarCodec<Set<V>>
{
    private final ScalarCodec<V> child;

    public SetScalarCodec(ScalarCodec<V> child, int maxLength)
    {
        super(maxLength);
        this.child = checkNotNull(child);
    }

    public SetScalarCodec(ScalarCodec<V> child)
    {
        this(child, DEFAULT_MAX_LENGTH);
    }

    @Override
    public void encode(Set<V> value, Output output)
    {
        encodeSize(value.size(), output);
        for (V item : value) {
            child.encode(item, output);
        }
    }

    @Override
    public Set<V> decode(Input input)
    {
        int sz = decodeSize(input);
        ImmutableSet.Builder<V> builder = ImmutableSet.builderWithExpectedSize(sz);
        for (int i = 0; i < sz; ++i) {
            builder.add(child.decode(input));
        }
        return builder.build();
    }
}
