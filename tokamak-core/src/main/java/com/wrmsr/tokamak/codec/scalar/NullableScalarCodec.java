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

import static com.google.common.base.Preconditions.checkNotNull;

public final class NullableScalarCodec<V>
        implements ScalarCodec<V>
{
    private final ScalarCodec<V> child;

    public NullableScalarCodec(ScalarCodec<V> child)
    {
        this.child = checkNotNull(child);
    }

    @Override
    public void encode(V value, Output output)
    {
        if (value != null) {
            output.putLong(0);
            child.encode(value, output);
        }
        else {
            output.putLong(1);
        }
    }

    @Override
    public V decode(Input input)
    {
        if (input.get() == 0) {
            return child.decode(input);
        }
        else {
            return null;
        }
    }
}
