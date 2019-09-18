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

import com.wrmsr.tokamak.serde.ByteArrayInput;
import com.wrmsr.tokamak.serde.ByteArrayOutput;
import com.wrmsr.tokamak.serde.Input;
import com.wrmsr.tokamak.serde.Output;
import com.wrmsr.tokamak.serde.Width;

public interface ValueSerde<V>
{
    /*
    TODO:
     - length awareness
     - packed
     - salting
     - vints
     - bulks (for denorms)
     - back-loaded lengths (lexical sorting, currently length-prefixed - is this even bad?)
      - double-ended output (same interface but paired, one reverses)
      - alt: compress keys & fuck locality lol
    */

    default Width getWidth()
    {
        return Width.any();
    }

    default boolean isNullable()
    {
        return false;
    }

    void write(V value, Output output);

    default byte[] writeBytes(V value)
    {
        ByteArrayOutput output = new ByteArrayOutput();
        write(value, output);
        return output.toByteArray();
    }

    V read(Input input);

    default V readBytes(byte[] value)
    {
        return read(new ByteArrayInput(value));
    }
}
