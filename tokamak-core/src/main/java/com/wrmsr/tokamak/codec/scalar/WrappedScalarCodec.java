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
import com.wrmsr.tokamak.codec.Width;
import com.wrmsr.tokamak.util.codec.Codec;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class WrappedScalarCodec<F, T>
        implements ScalarCodec<F>
{
    private final Codec<F, T> codec;
    private final ScalarCodec<T> child;

    public WrappedScalarCodec(Codec<F, T> codec, ScalarCodec<T> child)
    {
        this.codec = checkNotNull(codec);
        this.child = checkNotNull(child);
    }

    @Override
    public Width getWidth()
    {
        return child.getWidth();
    }

    @Override
    public boolean isNullable()
    {
        return child.isNullable();
    }

    @Override
    public void encode(F value, Output output)
    {
        child.encode(codec.encode(value), output);
    }

    @Override
    public F decode(Input input)
    {
        return codec.decode(child.decode(input));
    }
}
