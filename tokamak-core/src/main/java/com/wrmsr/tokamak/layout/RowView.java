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
package com.wrmsr.tokamak.layout;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.Pairs;

import javax.annotation.concurrent.Immutable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Immutable
public final class RowView
        implements Map<String, Object>
{
    private final RowLayout rowLayout;
    private final Object[] attributes;

    public RowView(RowLayout rowLayout, Object[] attributes)
    {
        this.rowLayout = checkNotNull(rowLayout);
        this.attributes = checkNotNull(attributes);
        checkArgument(rowLayout.getFields().size() == attributes.length);
    }

    public RowLayout getRowLayout()
    {
        return rowLayout;
    }

    public Object[] getAttributes()
    {
        return attributes;
    }

    @Override
    public int size()
    {
        return rowLayout.getFields().size();
    }

    @Override
    public boolean isEmpty()
    {
        return rowLayout.getFields().isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return rowLayout.getFieldSet().contains(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        for (Object att : attributes) {
            if (value == null) {
                if (att == null) {
                    return true;
                }
            }
            else {
                if (value.equals(att)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object get(Object key)
    {
        return attributes[rowLayout.getPositionsByField().get(key)];
    }

    @Override
    public Object put(String key, Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet()
    {
        return rowLayout.getFieldSet();
    }

    @Override
    public Collection<Object> values()
    {
        return ImmutableList.copyOf(attributes);
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return IntStream.range(0, rowLayout.getFields().size()).mapToObj(
                i -> new Pairs.Immutable<String, Object>(rowLayout.getFields().get(i), attributes[i])
        ).collect(toImmutableSet());
    }
}
