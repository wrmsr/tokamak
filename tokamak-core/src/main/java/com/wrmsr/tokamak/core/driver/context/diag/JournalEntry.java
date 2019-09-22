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
package com.wrmsr.tokamak.core.driver.context.diag;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.driver.state.State;
import com.wrmsr.tokamak.core.plan.node.Node;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JournalEntry
{
    public enum InvalidateReason
    {
        SYNC,
        DIRECT,
        LINKED,
        SCAN,
        RECURSIVE,
    }

    public static final class Invalidate
            extends JournalEntry
    {
        private final Node node;
        private final Set<Id> ids;
        private final InvalidateReason reason;

        public Invalidate(Node node, Set<Id> ids, InvalidateReason reason)
        {
            this.node = checkNotNull(node);
            this.ids = ImmutableSet.copyOf(ids);
            this.reason = checkNotNull(reason);
        }
    }

    public static final class BuildInput
            extends JournalEntry
    {
        private final Node node;
        private final Key key;

        public BuildInput(Node node, Key key)
        {
            this.node = checkNotNull(node);
            this.key = checkNotNull(key);
        }
    }

    public static abstract class BuildOutput
            extends JournalEntry
    {
        private final Node node;
        private final Key key;
        private final Collection<DriverRow> rows;

        public BuildOutput(Node node, Key key, Collection<DriverRow> rows)
        {
            this.node = checkNotNull(node);
            this.key = checkNotNull(key);
            this.rows = checkNotNull(rows);
        }
    }

    public static final class RowCachedBuildOutput
            extends BuildOutput
    {
        public RowCachedBuildOutput(Node node, Key key, Collection<DriverRow> rows)
        {
            super(node, key, rows);
        }
    }

    public static final class StateCachedBuildOutput
            extends BuildOutput
    {
        private final State state;

        public StateCachedBuildOutput(Node node, Key key, Collection<DriverRow> rows, State state)
        {
            super(node, key, rows);
            this.state = checkNotNull(state);
        }
    }

    public static final class UncachedBuildOutput
            extends BuildOutput
    {
        public UncachedBuildOutput(Node node, Key key, Collection<DriverRow> rows)
        {
            super(node, key, rows);
        }
    }

    public static final class DenormalizedInputJournalEntry
            extends JournalEntry
    {

    }
}
