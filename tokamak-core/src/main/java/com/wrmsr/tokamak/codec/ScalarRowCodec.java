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
package com.wrmsr.tokamak.codec;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ScalarRowCodec<V>
        implements RowCodec
{
    @Immutable
    public static final class FunctionPair<V>
    {
        private final BiConsumer<V, Output> encoder;
        private final Function<Input, V> decoder;

        public FunctionPair(BiConsumer<V, Output> encoder, Function<Input, V> decoder)
        {
            this.encoder = checkNotNull(encoder);
            this.decoder = checkNotNull(decoder);
        }

        public static <V> FunctionPair<V> of(BiConsumer<V, Output> encoder, Function<Input, V> decoder)
        {
            return new FunctionPair<>(encoder, decoder);
        }

        public BiConsumer<V, Output> getEncoder()
        {
            return encoder;
        }

        public Function<Input, V> getDecoder()
        {
            return decoder;
        }
    }

    private final String field;
    private final BiConsumer<V, Output> encoder;
    private final Function<Input, V> decoder;

    public ScalarRowCodec(String field, BiConsumer<V, Output> encoder, Function<Input, V> decoder)
    {
        this.field = checkNotNull(field);
        this.encoder = checkNotNull(encoder);
        this.decoder = checkNotNull(decoder);
    }

    public ScalarRowCodec(String field, FunctionPair<V> pair)
    {
        this(field, pair.encoder, pair.decoder);
    }

    public String getField()
    {
        return field;
    }

    @Override
    public void encode(Map<String, Object> row, Output output)
    {
        encoder.accept((V) row.get(field), output);
    }

    @Override
    public void decode(Sink sink, Input input)
    {
        sink.put(field, decoder.apply(input));
    }
}
