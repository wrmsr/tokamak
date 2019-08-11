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

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class MoreStrings
{
    private MoreStrings()
    {
    }

    public static String[] splitProperty(String prop)
    {
        int pos = prop.indexOf("=");
        checkArgument(pos > 0);
        return new String[] {prop.substring(0, pos), prop.substring(pos + 1)};
    }

    public static Map<String, String> getSystemProperties()
    {
        return System.getProperties().entrySet().stream()
                .map(e -> new Pair.Immutable<>((String) e.getKey(), (String) e.getValue()))
                .collect(toImmutableMap());
    }
}
