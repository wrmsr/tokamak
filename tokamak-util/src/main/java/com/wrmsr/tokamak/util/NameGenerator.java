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

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NameGenerator
        implements Supplier<String>
{
    public static final String DEFAULT_PREFIX = "_";

    private final Set<String> names;
    private final String globalPrefix;
    private final Map<String, Integer> prefixCounts = new HashMap<>();

    public NameGenerator(Set<String> unavailableStrings, String globalPrefix)
    {
        this.names = ImmutableSet.copyOf(unavailableStrings);
        this.globalPrefix = checkNotNull(globalPrefix);
    }

    public NameGenerator(Set<String> names)
    {
        this(names, DEFAULT_PREFIX);
    }

    public NameGenerator()
    {
        this(ImmutableSet.of());
    }

    public String getGlobalPrefix()
    {
        return globalPrefix;
    }

    public String get(String prefix)
    {
        while (true) {
            int count = prefixCounts.getOrDefault(prefix, 0);
            prefixCounts.put(prefix, count + 1);
            String name = globalPrefix + count;
            if (!names.contains(name)) {
                return name;
            }
        }
    }

    @Override
    public String get()
    {
        return get("");
    }
}
