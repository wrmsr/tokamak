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
package com.wrmsr.tokamak.core.layout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;

@Immutable
public final class TableLayout
{
    @Immutable
    public static final class Key
            implements StreamableIterable<String>
    {
        private final List<String> fields;

        public Key(List<String> fields)
        {
            this.fields = ImmutableList.copyOf(fields);
            checkArgument(!this.fields.isEmpty());
            checkUnique(this.fields);
        }

        @Override
        public String toString()
        {
            return "Key{" +
                    "fields=" + fields +
                    '}';
        }

        @JsonValue
        public List<String> getFields()
        {
            return fields;
        }

        @JsonCreator
        public static Key of(Iterable<String> fields)
        {
            return new Key(ImmutableList.copyOf(fields));
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

    @JsonCreator
    public TableLayout(
            @JsonProperty("rowLayout") RowLayout rowLayout,
            @JsonProperty("primaryKey") Key primaryKey,
            @JsonProperty("secondaryKeys") List<Key> secondaryKeys)
    {
        this.rowLayout = checkNotNull(rowLayout);
        this.primaryKey = checkNotNull(primaryKey);
        this.secondaryKeys = ImmutableList.copyOf(secondaryKeys);
        primaryKey.forEach(f -> checkArgument(rowLayout.getFields().contains(f)));
        this.secondaryKeys.forEach(k -> k.forEach(f -> checkArgument(rowLayout.getFields().contains(f))));
    }

    @JsonProperty("rowLayout")
    public RowLayout getRowLayout()
    {
        return rowLayout;
    }

    @JsonProperty("primaryKey")
    public Key getPrimaryKey()
    {
        return primaryKey;
    }

    public Set<String> getPrimaryKeyFields()
    {
        return ImmutableSet.copyOf(primaryKey);
    }

    @JsonProperty("secondaryKeys")
    public List<Key> getSecondaryKeys()
    {
        return secondaryKeys;
    }
}
