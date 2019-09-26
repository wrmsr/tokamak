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

import com.wrmsr.tokamak.core.serde.Input;
import com.wrmsr.tokamak.core.serde.Output;
import com.wrmsr.tokamak.core.serde.Serde;
import com.wrmsr.tokamak.core.serde.Width;

import javax.annotation.concurrent.Immutable;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class FunctionPairSerde<V>
        implements Serde<V>
{
    private final BiConsumer<V, Output> encoder;
    private final Function<Input, V> decoder;
    private final Width width;
    private final boolean isNullable;

    public FunctionPairSerde(
            BiConsumer<V, Output> encoder,
            Function<Input, V> decoder,
            Width width,
            boolean isNullable)
    {
        this.encoder = checkNotNull(encoder);
        this.decoder = checkNotNull(decoder);
        this.width = checkNotNull(width);
        this.isNullable = isNullable;
    }

    public FunctionPairSerde(
            BiConsumer<V, Output> encoder,
            Function<Input, V> decoder)
    {
        this(encoder, decoder, Width.any(), false);
    }

    public static <V> FunctionPairSerde<V> of(
            BiConsumer<V, Output> encoder,
            Function<Input, V> decoder,
            Width width,
            boolean isNullable)
    {
        return new FunctionPairSerde<>(encoder, decoder, width, isNullable);
    }

    public static <V> FunctionPairSerde<V> of(
            BiConsumer<V, Output> encoder,
            Function<Input, V> decoder)
    {
        return new FunctionPairSerde<>(encoder, decoder);
    }

    public BiConsumer<V, Output> getEncoder()
    {
        return encoder;
    }

    public Function<Input, V> getDecoder()
    {
        return decoder;
    }

    @Override
    public Width getWidth()
    {
        return width;
    }

    @Override
    public boolean isNullable()
    {
        return isNullable;
    }

    @Override
    public void write(V value, Output output)
    {
        encoder.accept(value, output);
    }

    @Override
    public V read(Input input)
    {
        return decoder.apply(input);
    }
}
