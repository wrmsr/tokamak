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
import java.util.HashSet;
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
    private final boolean useGlobalPrefixIfPresent;
    private final boolean addGlobalPrefixBeforeNumber;

    private final Map<String, Integer> nameCounts = new HashMap<>();
    private final Object lock = new Object();

    public NameGenerator(
            Set<String> unavailableStrings,
            String globalPrefix,
            boolean useGlobalPrefixIfPresent,
            boolean addGlobalPrefixBeforeNumber)
    {
        this.names = new HashSet<>();
        names.addAll(unavailableStrings);
        this.globalPrefix = checkNotNull(globalPrefix);
        this.useGlobalPrefixIfPresent = useGlobalPrefixIfPresent;
        this.addGlobalPrefixBeforeNumber = addGlobalPrefixBeforeNumber;
    }

    public NameGenerator(Set<String> names, String globalPrefix)
    {
        this(names, globalPrefix, true, true);
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
        String baseName;
        if (useGlobalPrefixIfPresent && prefix.startsWith(globalPrefix)) {
            baseName = prefix;
        }
        else {
            baseName = globalPrefix + prefix;
        }

        int baseCount = -1;
        if (Character.isDigit(baseName.charAt(baseName.length() - 1))) {
            int i = baseName.length() - 2;
            while (i >= 0 && Character.isDigit(baseName.charAt(i))) {
                --i;
            }
            i += 1;
            baseCount = Integer.parseInt(baseName.substring(i), 10);
            baseName = baseName.substring(0, i);
        }

        if (addGlobalPrefixBeforeNumber) {
            if (!(useGlobalPrefixIfPresent && baseName.endsWith(globalPrefix))) {
                baseName += globalPrefix;
            }
        }

        synchronized (lock) {
            if (baseCount >= 0) {
                int count = nameCounts.getOrDefault(baseName, 0);
                if (baseCount > count) {
                    nameCounts.put(baseName, baseCount);
                }
            }

            while (true) {
                int count = nameCounts.getOrDefault(baseName, 0);
                nameCounts.put(baseName, count + 1);
                String name = baseName + count;
                if (!names.contains(name)) {
                    names.add(name);
                    return name;
                }
            }
        }
    }

    @Override
    public String get()
    {
        return get("");
    }
}
