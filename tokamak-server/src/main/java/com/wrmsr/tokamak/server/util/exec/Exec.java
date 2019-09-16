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
package com.wrmsr.tokamak.server.util.exec;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Exec
{
    void exec(String path, List<String> args, Map<String, String> env)
            throws IOException;

    default void exec(String path, List<String> args)
            throws IOException
    {
        exec(path, args, currentEnv());
    }

    static Map<String, String> currentEnv(Map<String, String> updates)
    {
        Map<String, String> env = new LinkedHashMap<>(System.getenv());
        updates.forEach((k, v) -> {
            if (v != null) {
                env.put(k, v);
            }
            else {
                env.remove(k);
            }
        });
        return env;
    }

    static Map<String, String> currentEnv()
    {
        return currentEnv(ImmutableMap.of());
    }
}
