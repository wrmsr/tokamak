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
package com.wrmsr.tokamak.main.util.exec;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Map;

public abstract class AbstractExec
        implements Exec
{
    public static String[] convertEnv(Map<String, String> env)
    {
        if (env == null) {
            return null;
        }
        ArrayList<String> ret = Lists.newArrayList(Iterables.transform(
                env.entrySet(), entry -> String.format("%s=%s", entry.getKey(), entry.getValue())));
        return ret.toArray(new String[ret.size()]);
    }
}
