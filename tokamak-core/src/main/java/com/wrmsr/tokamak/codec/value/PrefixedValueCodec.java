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

package com.wrmsr.tokamak.codec.value;

import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;
import com.wrmsr.tokamak.codec.Width;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PrefixedValueCodec<V>
        implements ValueCodec<V>
{
    @FunctionalInterface
    public interface MismatchDecoder<V>
    {
        V decode(byte[] prefix, Input input);
    }

    private final byte[] prefix;
    private final ValueCodec<V> child;
    private final MismatchDecoder<V> mismatchDecoder;

    public PrefixedValueCodec(byte[] prefix, ValueCodec<V> child, MismatchDecoder<V> mismatchDecoder)
    {
        this.prefix = checkNotNull(prefix).clone();
        this.child = checkNotNull(child);
        this.mismatchDecoder = checkNotNull(mismatchDecoder);
    }

    public PrefixedValueCodec(byte[] prefix, ValueCodec<V> child)
    {
        this(prefix, child, (p, i) -> {throw new IllegalStateException("Prefix mismatch: " + Arrays.toString(p));});
    }

    public byte[] getPrefix()
    {
        return prefix;
    }

    public ValueCodec<V> getChild()
    {
        return child;
    }

    public MismatchDecoder getMismatchDecoder()
    {
        return mismatchDecoder;
    }

    @Override
    public Width getWidth()
    {
        return child.getWidth().map(w -> w + prefix.length);
    }

    @Override
    public boolean isNullable()
    {
        return child.isNullable();
    }

    @Override
    public void encode(V value, Output output)
    {
        output.putBytes(prefix);
        child.encode(value, output);
    }

    @Override
    public V decode(Input input)
    {
        byte[] prefix = input.getBytes(this.prefix.length);
        if (!Arrays.equals(prefix, this.prefix)) {
            return mismatchDecoder.decode(prefix, input);
        }
        else {
            return child.decode(input);
        }
    }
}
