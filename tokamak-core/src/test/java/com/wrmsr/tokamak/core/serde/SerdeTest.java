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
package com.wrmsr.tokamak.core.serde;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.serde.impl.FixedKeyObjectMapSerde;
import com.wrmsr.tokamak.core.serde.impl.VariableLengthSerde;
import com.wrmsr.tokamak.util.OpenByteArrayOutputStream;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.Map;

public class SerdeTest
        extends TestCase
{
    public void testByteBuffers()
            throws Throwable
    {
        OpenByteArrayOutputStream bas = new OpenByteArrayOutputStream();
        bas.write(new byte[] {(byte) 1, (byte) 2, (byte) 3});
        System.out.println(bas);
        bas.getBuf()[0] = (byte) 4;
        System.out.println(bas);
        ByteBuffer.wrap(bas.getBuf()).putShort((short) 0x1234);
        System.out.println(bas);
    }

    public void testFixedKeyObjectMapValueSerde()
            throws Throwable
    {
        FixedKeyObjectMapSerde<String> codec = new FixedKeyObjectMapSerde<>(ImmutableMap.of(
                "long", Serdes.LONG_VALUE_SERDE,
                "string", new VariableLengthSerde<>(Serdes.STRING_VALUE_SERDE)
        ), false);

        Map<String, Object> expectedMap = ImmutableMap.of(
                "long", 420L,
                "string", "abcd"
        );

        byte[] bytes = codec.writeBytes(expectedMap);

        Map<String, Object> givenMap = codec.readBytes(bytes);

        assertEquals(expectedMap, givenMap);
    }
}
