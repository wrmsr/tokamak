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

import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public final class NameGenerator
        implements Supplier<String>
{
    public static final String DEFAULT_PREFIX = "__anon$";

    private final Set<String> names;
    private final String prefix;

    private int counter;

    public NameGenerator(Set<String> unavailableStrings, String prefix, int initialCount)
    {
        checkArgument(initialCount >= 0);
        this.names = unmodifiableSet(requireNonNull(unavailableStrings));
        this.prefix = requireNonNull(prefix);
        counter = initialCount;
    }

    public NameGenerator(Set<String> names, String prefix)
    {
        this(names, prefix, 0);
    }

    public NameGenerator(Set<String> names)
    {
        this(names, DEFAULT_PREFIX);
    }

    public NameGenerator()
    {
        this(ImmutableSet.of());
    }

    public int getCounter()
    {
        return counter;
    }

    public String getPrefix()
    {
        return prefix;
    }

    @Override
    public String get()
    {
        while (true) {
            String name = prefix + counter++;
            if (!names.contains(name)) {
                return name;
            }
        }
    }
}
