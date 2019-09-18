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

import com.wrmsr.tokamak.serde.Input;
import com.wrmsr.tokamak.serde.Output;
import com.wrmsr.tokamak.serde.Width;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreOptionals.mapOptional;

@Immutable
public final class NullableValueSerde<V>
        implements ValueSerde<V>
{
    private final ValueSerde<V> child;

    public NullableValueSerde(ValueSerde<V> child)
    {
        this.child = checkNotNull(child);
    }

    public static <V> ValueSerde<V> of(ValueSerde<V> child)
    {
        if (child.isNullable()) {
            return child;
        }
        else {
            return new NullableValueSerde<>(child);
        }
    }

    @Override
    public Width getWidth()
    {
        return Width.of(1, mapOptional(child.getWidth().getMax(), v -> v + 1));
    }

    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public void write(V value, Output output)
    {
        if (value != null) {
            output.put((byte) 0);
            child.write(value, output);
        }
        else {
            output.put((byte) 1);
        }
    }

    @Override
    public V read(Input input)
    {
        if (input.get() == (byte) 0) {
            return child.read(input);
        }
        else {
            return null;
        }
    }
}
