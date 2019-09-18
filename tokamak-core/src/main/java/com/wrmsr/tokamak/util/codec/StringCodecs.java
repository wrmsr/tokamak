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

import com.google.common.base.Charsets;

import java.nio.charset.Charset;

public class StringCodecs
{
    private StringCodecs()
    {
    }

    public static final class StringCodec
            implements Codec<String, byte[]>
    {
        private final Charset charset;

        public StringCodec(Charset charset)
        {
            this.charset = charset;
        }

        @Override
        public final byte[] encode(String value)
        {
            return value.getBytes(charset);
        }

        @Override
        public final String decode(byte[] value)
        {
            return new String(value, charset);
        }
    }

    public static final StringCodec UTF8_CODEC = new StringCodec(Charsets.UTF_8);
}
