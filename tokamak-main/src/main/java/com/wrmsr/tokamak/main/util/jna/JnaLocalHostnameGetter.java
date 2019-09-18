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
package com.wrmsr.tokamak.main.util.jna;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.wrmsr.tokamak.main.util.dns.LocalHostnameGetter;

public final class JnaLocalHostnameGetter
    implements LocalHostnameGetter
{
    private interface Libc
            extends Library
    {
        int gethostname(byte[] name, int size_t)
                throws LastErrorException;
    }

    @Override
    public String get()
    {
        Libc libc = Native.load("c", Libc.class);
        byte[] hostname = new byte[256];
        libc.gethostname(hostname, hostname.length);
        return Native.toString(hostname);
    }
}
