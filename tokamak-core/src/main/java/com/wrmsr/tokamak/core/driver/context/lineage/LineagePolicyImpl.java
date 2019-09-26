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

import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class LineagePolicyImpl
        implements LineagePolicy
{
    private final LineageRetention retention;
    private final LineageGranularity granularity;

    public LineagePolicyImpl(LineageRetention retention, LineageGranularity granularity)
    {
        this.retention = checkNotNull(retention);
        this.granularity = checkNotNull(granularity);
    }

    @Override
    public Set<LineageEntry> build(Iterator<DriverRow> rows)
    {
        // FIXME: Granularity.ID + Retention.MINIMAL optimization
        ImmutableSet.Builder<LineageEntry> builder = ImmutableSet.builder();
        retention.consume(rows, new LineageRetention.Sink()
        {
            @Override
            public void accept(DriverRow row)
            {
                builder.add(granularity.newEntry(row));
            }

            @Override
            public void accept(Set<LineageEntry> lineage)
            {
                builder.addAll(lineage);
            }
        });
        return builder.build();
    }
}
