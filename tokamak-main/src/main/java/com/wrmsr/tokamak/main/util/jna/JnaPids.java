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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.wrmsr.tokamak.main.util.pid.Pids;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

public final class JnaPids
        implements Pids
{
    private interface Libc
            extends Library
    {
        int getpid();
    }

    private final SupplierLazyValue<Long> pid = new SupplierLazyValue<>();

    @Override
    public long get()
    {
        return pid.get(() -> {
            Libc libc = Native.load((Platform.isWindows() ? "msvcrt" : "c"), Libc.class);
            return libc.getpid() & 0xFFFFFFFFL;
        });
    }
}
