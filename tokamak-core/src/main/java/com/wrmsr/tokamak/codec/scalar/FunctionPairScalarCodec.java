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

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FunctionPairScalarCodec<V>
        implements ScalarCodec<V>
{
    private final BiConsumer<V, Output> encoder;
    private final Function<Input, V> decoder;

    public FunctionPairScalarCodec(BiConsumer<V, Output> encoder, Function<Input, V> decoder)
    {
        this.encoder = checkNotNull(encoder);
        this.decoder = checkNotNull(decoder);
    }

    public static <V> FunctionPairScalarCodec<V> of(BiConsumer<V, Output> encoder, Function<Input, V> decoder)
    {
        return new FunctionPairScalarCodec<>(encoder, decoder);
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
    public void encode(V value, Output output)
    {
        encoder.accept(value, output);
    }

    @Override
    public V decode(Input input)
    {
        return decoder.apply(input);
    }
}