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

import java.util.OptionalLong;

public final class MoreSystem
{
    private MoreSystem()
    {
    }

    public static OptionalLong getPid()
    {
        try {
            Class<?> cls = Class.forName("java.lang.ProcessHandle");
            Object current = cls.getDeclaredMethod("current").invoke(null);
            long pid = (long) cls.getDeclaredMethod("pid").invoke(current);
            return OptionalLong.of(pid);
        }
        catch (ReflectiveOperationException e) {
            return OptionalLong.empty();
        }
    }

    public static String shellEscape(String s)
    {
        return "'" + s.replace("'", "'\"'\"'") + "'";
    }
}
