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

import javax.annotation.concurrent.Immutable;

import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreOptionals.mapOptional;

@Immutable
public final class NullableScalarCodec<V>
        implements ScalarCodec<V>
{
    private final ScalarCodec<V> child;

    public NullableScalarCodec(ScalarCodec<V> child)
    {
        this.child = checkNotNull(child);
    }

    public static <V> ScalarCodec<V> of(ScalarCodec<V> child)
    {
        if (child.isNullable()) {
            return child;
        }
        else {
            return new NullableScalarCodec<>(child);
        }
    }

    @Override
    public OptionalInt getLength()
    {
        return mapOptional(child.getLength(), l -> l + 1);
    }

    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public void encode(V value, Output output)
    {
        if (value != null) {
            output.put((byte) 0);
            child.encode(value, output);
        }
        else {
            output.put((byte) 1);
        }
    }

    @Override
    public V decode(Input input)
    {
        if (input.get() == (byte) 0) {
            return child.decode(input);
        }
        else {
            return null;
        }
    }
}
