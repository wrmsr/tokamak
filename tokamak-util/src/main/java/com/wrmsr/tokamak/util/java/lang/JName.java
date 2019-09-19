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
package com.wrmsr.tokamak.util.java.lang;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.compareIterators;

@Immutable
public final class JName
        implements StreamableIterable<String>, Comparable<JName>
{
    private final List<String> parts;

    public JName(List<String> parts)
    {
        checkNotNull(parts);
        checkArgument(!parts.isEmpty());
        this.parts = ImmutableList.copyOf(parts);
    }

    public static JName of(String name)
    {
        return new JName(ImmutableList.of(name));
    }

    public static JName of(Object... parts)
    {
        return new JName(Arrays.stream(parts).flatMap(o -> {
            if (o instanceof JName) {
                return ((JName) o).getParts().stream();
            }
            else if (o instanceof String) {
                return Stream.of((String) o);
            }
            else if (o instanceof Class) {
                return Splitter.on(".").splitToList(((Class) o).getCanonicalName()).stream();
            }
            else {
                throw new IllegalArgumentException();
            }
        }).collect(toImmutableList()));
    }

    public static JName parse(String str)
    {
        return new JName(Splitter.on('.').splitToList(str));
    }

    public List<String> getParts()
    {
        return parts;
    }

    @Override
    public Iterator<String> iterator()
    {
        return parts.iterator();
    }

    public int size()
    {
        return parts.size();
    }

    public String get(int index)
    {
        return parts.get(index);
    }

    @Override
    public int compareTo(JName o)
    {
        return compareIterators(parts.iterator(), o.getParts().iterator());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JName that = (JName) o;
        return Objects.equals(parts, that.parts);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(parts);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("parts", parts)
                .toString();
    }

    public String join()
    {
        return Joiner.on('.').join(parts);
    }

    public boolean startsWith(JName prefix)
    {
        if (size() < prefix.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); ++i) {
            if (!parts.get(i).equals(prefix.get(i))) {
                return false;
            }
        }
        return true;
    }
}
