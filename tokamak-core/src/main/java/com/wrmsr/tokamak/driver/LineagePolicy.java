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
package com.wrmsr.tokamak.driver;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.plan.node.StatefulNode;

import java.util.Set;
import java.util.stream.StreamSupport;

public interface LineagePolicy
{
    Set<DriverRow> build(DriverRow... rows);

    default Set<DriverRow> build(Iterable<DriverRow> rows)
    {
        return build(StreamSupport.stream(rows.spliterator(), false).toArray(DriverRow[]::new));
    }

    LineagePolicy NOP = new LineagePolicy()
    {
        @Override
        public Set<DriverRow> build(DriverRow... rows)
        {
            return ImmutableSet.of();
        }
    };

    LineagePolicy MINIMAL = new LineagePolicy()
    {
        @Override
        public Set<DriverRow> build(DriverRow... rows)
        {
            ImmutableSet.Builder<DriverRow> set = ImmutableSet.builder();
            for (DriverRow row : rows) {
                if (row.getNode() instanceof StatefulNode) {
                    set.add(row);
                }
                else {
                    set.addAll(row.getLineage());
                }
            }
            return set.build();
        }
    };

    LineagePolicy CASCADE = new LineagePolicy()
    {
        @Override
        public Set<DriverRow> build(DriverRow... rows)
        {
            ImmutableSet.Builder<DriverRow> set = ImmutableSet.builder();
            for (DriverRow row : rows) {
                if (row.getNode() instanceof StatefulNode) {
                    set.add(row);
                }
                else {
                    set.add(row);
                    set.addAll(row.getLineage());
                }
            }
            return set.build();
        }
    };

    LineagePolicy FULL = new LineagePolicy()
    {
        @Override
        public Set<DriverRow> build(DriverRow... rows)
        {
            ImmutableSet.Builder<DriverRow> set = ImmutableSet.builder();
            for (DriverRow row : rows) {
                set.add(row);
                set.addAll(row.getLineage());
            }
            return set.build();
        }
    };
}
