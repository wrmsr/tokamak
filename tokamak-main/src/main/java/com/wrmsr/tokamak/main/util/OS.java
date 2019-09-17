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
package com.wrmsr.tokamak.main.util;

import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

public enum OS
{
    LINUX,
    MAC,
    WINDOWS,
    UNKNOWN,
    ;

    public static OS fromName(String name)
    {
        if (name.startsWith("Linux") || name.startsWith("LINUX")) {
            return OS.LINUX;
        }
        else if (name.startsWith("Mac")) {
            return OS.MAC;
        }
        else if (name.startsWith("Windows")) {
            return OS.WINDOWS;
        }
        else {
            return UNKNOWN;
        }
    }

    private static final SupplierLazyValue<OS> current = new SupplierLazyValue<>();

    public static OS get()
    {
        return current.get(() -> fromName(System.getProperty("os.name", "")));
    }
}
