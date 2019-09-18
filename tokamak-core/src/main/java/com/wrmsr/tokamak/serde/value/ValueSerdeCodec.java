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
package com.wrmsr.tokamak.serde.value;

import com.wrmsr.tokamak.util.codec.Codec;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ValueSerdeCodec<T>
        implements Codec<T, byte[]>
{
    private final ValueSerde<T> serde;

    public ValueSerdeCodec(ValueSerde<T> serde)
    {
        this.serde = checkNotNull(serde);
    }

    @Override
    public T decode(byte[] data)
    {
        return serde.readBytes(data);
    }

    @Override
    public byte[] encode(T data)
    {
        return serde.writeBytes(data);
    }
}
