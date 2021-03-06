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
package com.wrmsr.tokamak.api;

final class Util
{
    private Util()
    {
    }

    static <T> T checkNotNull(T obj)
    {
        if (obj == null) {
            throw new IllegalStateException();
        }
        return obj;
    }

    static void checkArgument(boolean state)
    {
        if (!state) {
            throw new IllegalStateException();
        }
    }

    static void checkState(boolean state)
    {
        if (!state) {
            throw new IllegalStateException();
        }
    }

    static String checkNotEmpty(String obj)
    {
        checkNotNull(obj);
        checkState(!obj.isEmpty());
        return obj;
    }

    static String toHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static byte[] fromHex(String string)
    {
        checkArgument(string.length() % 2 == 0);
        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = Byte.parseByte(string.substring(i * 2, (i + 1) * 2));
        }
        return bytes;
    }
}
