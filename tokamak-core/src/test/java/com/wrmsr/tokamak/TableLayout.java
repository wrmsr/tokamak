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
package com.wrmsr.tokamak;

import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.List;

@Immutable
public class TableLayout
{
    @Immutable
    public static final class Key
            implements Iterable<String>
    {
        private final List<String> fields;

        public Key(List<String> fields)
        {
            this.fields = ImmutableList.copyOf(fields);
        }

        @Override
        public String toString()
        {
            return "Key{" +
                    "fields=" + fields +
                    '}';
        }

        public List<String> getFields()
        {
            return fields;
        }

        @Override
        public Iterator<String> iterator()
        {
            return fields.iterator();
        }
    }

    private final RowLayout rowLayout;
    private final Key primaryKey;
    private final List<Key> secondaryKeys;

    public TableLayout(RowLayout rowLayout, Key primaryKey, List<Key> secondaryKeys)
    {
        this.rowLayout = rowLayout;
        this.primaryKey = primaryKey;
        this.secondaryKeys = ImmutableList.copyOf(secondaryKeys);
    }

    public RowLayout getRowLayout()
    {
        return rowLayout;
    }

    public Key getPrimaryKey()
    {
        return primaryKey;
    }

    public List<Key> getSecondaryKeys()
    {
        return secondaryKeys;
    }
}
