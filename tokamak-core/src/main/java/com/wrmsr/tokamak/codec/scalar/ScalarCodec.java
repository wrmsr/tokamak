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

import com.wrmsr.tokamak.codec.ByteArrayInput;
import com.wrmsr.tokamak.codec.ByteArrayOutput;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;

public interface ScalarCodec<V>
{
    void encode(V value, Output output);

    default byte[] encodeBytes(V value)
    {
        ByteArrayOutput output = new ByteArrayOutput();
        encode(value, output);
        return output.toByteArray();
    }

    V decode(Input input);

    default V decodeBytes(byte[] value)
    {
        return decode(new ByteArrayInput(value));
    }
}
