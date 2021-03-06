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
package com.wrmsr.tokamak.util.codec;

public interface Codec<F, T>
        extends Encoder<F, T>, Decoder<F, T>
{
    default F roundTrip(F obj)
    {
        return decode(encode(obj));
    }

    default T reverseRoundTrip(T obj)
    {
        return encode(decode(obj));
    }

    static <T> Codec<T, T> identity()
    {
        return new Codec<T, T>()
        {
            @Override
            public T decode(T value)
            {
                return value;
            }

            @Override
            public T encode(T value)
            {
                return value;
            }
        };
    }

    static <F, T> Codec<F, T> of(Encoder<F, T> encoder, Decoder<F, T> decoder)
    {
        return new Codec<F, T>()
        {
            @Override
            public F decode(T value)
            {
                return decoder.decode(value);
            }

            @Override
            public T encode(F value)
            {
                return encoder.encode(value);
            }
        };
    }

    static <F, T> Codec<F, T> flip(Codec<T, F> codec)
    {
        return new Codec<F, T>()
        {
            @Override
            public F decode(T value)
            {
                return codec.encode(value);
            }

            @Override
            public T encode(F value)
            {
                return codec.decode(value);
            }
        };
    }

    static <F, M0, T> Codec<F, T> compose(Codec<F, M0> c0, Codec<M0, T> c1)
    {
        return new Codec<F, T>()
        {
            @Override
            public F decode(T value)
            {
                return c0.decode(c1.decode(value));
            }

            @Override
            public T encode(F value)
            {
                return c1.encode(c0.encode(value));
            }
        };
    }

    static <F, M0, M1, T> Codec<F, T> compose(Codec<F, M0> c0, Codec<M0, M1> c1, Codec<M1, T> c2)
    {
        return new Codec<F, T>()
        {
            @Override
            public F decode(T value)
            {
                return c0.decode(c1.decode(c2.decode(value)));
            }

            @Override
            public T encode(F value)
            {
                return c2.encode(c1.encode(c0.encode(value)));
            }
        };
    }

    static <F, M0, M1, M2, T> Codec<F, T> compose(Codec<F, M0> c0, Codec<M0, M1> c1, Codec<M1, M2> c2, Codec<M2, T> c3)
    {
        return new Codec<F, T>()
        {
            @Override
            public F decode(T value)
            {
                return c0.decode(c1.decode(c2.decode(c3.decode(value))));
            }

            @Override
            public T encode(F value)
            {
                return c3.encode(c2.encode(c1.encode(c0.encode(value))));
            }
        };
    }

    static <F, M0, M1, M2, M3, T> Codec<F, T> compose(Codec<F, M0> c0, Codec<M0, M1> c1, Codec<M1, M2> c2, Codec<M2, M3> c3, Codec<M3, T> c4)
    {
        return new Codec<F, T>()
        {
            @Override
            public F decode(T value)
            {
                return c0.decode(c1.decode(c2.decode(c3.decode(c4.decode(value)))));
            }

            @Override
            public T encode(F value)
            {
                return c4.encode(c3.encode(c2.encode(c1.encode(c0.encode(value)))));
            }
        };
    }
}
