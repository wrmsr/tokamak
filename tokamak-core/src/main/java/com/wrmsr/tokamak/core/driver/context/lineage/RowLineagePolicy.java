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

package com.wrmsr.tokamak.core.driver.context.lineage;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.plan.node.Node;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RowLineagePolicy
        implements LineagePolicy
{
    @Immutable
    private static final class LineageImpl
            implements Lineage
    {
        @Immutable
        private static final class EntryImpl
                implements Entry
        {
            private final DriverRow row;

            public EntryImpl(DriverRow row)
            {
                this.row = checkNotNull(row);
            }

            @Override
            public boolean equals(Object o)
            {
                if (this == o) { return true; }
                if (o == null || getClass() != o.getClass()) { return false; }
                EntryImpl entrty = (EntryImpl) o;
                return Objects.equals(row, entrty.row);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(row);
            }

            @Override
            public Node getNode()
            {
                return row.getNode();
            }

            @Override
            public Id getId()
            {
                return row.getId();
            }
        }

        private final Set<EntryImpl> set;

        public LineageImpl(Set<EntryImpl> set)
        {
            this.set = ImmutableSet.copyOf(set);
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Set<Entry> getEntries()
        {
            return (Set) set;
        }
    }

    @Override
    public Retention getRetention()
    {
        return null;
    }

    @Override
    public Lineage build(DriverRow... rows)
    {
        return null;
    }
}
