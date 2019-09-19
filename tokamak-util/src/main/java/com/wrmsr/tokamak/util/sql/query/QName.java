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
package com.wrmsr.tokamak.util.sql.query;

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
public final class QName
        implements StreamableIterable<String>, Comparable<QName>
{
    private final List<String> parts;

    public QName(List<String> parts)
    {
        checkNotNull(parts);
        checkArgument(!parts.isEmpty());
        this.parts = ImmutableList.copyOf(parts);
    }

    public static QName of(String name)
    {
        return new QName(ImmutableList.of(name));
    }

    public static QName of(Object... parts)
    {
        return new QName(Arrays.stream(parts).flatMap(o -> {
            if (o instanceof QName) {
                return ((QName) o).getParts().stream();
            }
            else if (o instanceof String) {
                return Stream.of((String) o);
            }
            else {
                throw new IllegalArgumentException();
            }
        }).collect(toImmutableList()));
    }

    public static QName parse(String str)
    {
        return new QName(Splitter.on('.').splitToList(str));
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
    public int compareTo(QName o)
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
        QName that = (QName) o;
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

    public boolean startsWith(QName prefix)
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
