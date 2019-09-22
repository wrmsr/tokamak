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
package com.wrmsr.tokamak.core.serde.value;

import com.wrmsr.tokamak.core.serde.Input;
import com.wrmsr.tokamak.core.serde.Output;
import com.wrmsr.tokamak.core.serde.Width;
import com.wrmsr.tokamak.util.codec.Codec;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class CodecValueSerde<F, T>
        implements ValueSerde<F>
{
    private final Codec<F, T> codec;
    private final ValueSerde<T> child;

    public CodecValueSerde(Codec<F, T> codec, ValueSerde<T> child)
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
    public void write(F value, Output output)
    {
        child.write(codec.encode(value), output);
    }

    @Override
    public F read(Input input)
    {
        return codec.decode(child.read(input));
    }
}
