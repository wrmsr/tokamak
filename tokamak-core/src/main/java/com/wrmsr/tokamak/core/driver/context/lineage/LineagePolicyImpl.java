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
import com.wrmsr.tokamak.core.driver.DriverRow;

import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.arrayIterate;

public final class LineagePolicyImpl
        implements LineagePolicy
{
    private static final class LineageImpl
            implements Lineage
    {
        private Set<Entry> entries;

        public LineageImpl(Set<Entry> entries)
        {
            this.entries = ImmutableSet.copyOf(entries);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            LineageImpl entries1 = (LineageImpl) o;
            return Objects.equals(entries, entries1.entries);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(entries);
        }

        @Override
        public String toString()
        {
            return "LineageImpl{" +
                    "entries=" + entries +
                    '}';
        }

        @Override
        public Set<Entry> getEntries()
        {
            return entries;
        }
    }

    private final LineageRetention retention;
    private final LineageGranularity granularity;

    public LineagePolicyImpl(LineageRetention retention, LineageGranularity granularity)
    {
        this.retention = checkNotNull(retention);
        this.granularity = checkNotNull(granularity);
    }

    @Override
    public Lineage build(DriverRow... rows)
    {
        // FIXME: Granularity.ID + Retention.MINIMAL optimization
        ImmutableSet.Builder<Lineage.Entry> builder = ImmutableSet.builder();
        retention.consume(arrayIterate(rows), new LineageRetention.Sink()
        {
            @Override
            public void accept(DriverRow row)
            {
                builder.add(granularity.newEntry(row));
            }

            @Override
            public void accept(Lineage lineage)
            {
                builder.addAll(lineage);
            }
        });
        return new LineageImpl(builder.build());
    }
}
