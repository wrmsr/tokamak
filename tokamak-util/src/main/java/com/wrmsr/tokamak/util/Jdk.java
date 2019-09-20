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
package com.wrmsr.tokamak.util;

import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.lang.management.ManagementFactory;

public final class Jdk
{
    private Jdk()
    {
    }

    public static int parseSpecificationMajor(String spec)
    {
        String[] parts = spec.split("\\.");
        if (parts.length == 1) {
            return Integer.parseInt(spec);
        }
        else if (parts.length == 2 && parts[0].equals("1")) {
            return Integer.parseInt(parts[1]);
        }
        else {
            throw new IllegalArgumentException(spec);
        }
    }

    private static SupplierLazyValue<Integer> major = new SupplierLazyValue<>();

    public static int getMajor()
    {
        return major.get(() -> {
            String spec = System.getProperty("java.specification.version");
            return parseSpecificationMajor(spec);
        });
    }

    public static boolean isDebug()
    {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }
}
