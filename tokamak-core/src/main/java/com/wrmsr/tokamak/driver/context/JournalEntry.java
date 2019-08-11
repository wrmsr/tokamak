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
package com.wrmsr.tokamak.driver.context;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.node.Node;

import java.util.Set;

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

    public final class Invalidate
            extends JournalEntry
    {
        private final Node node;
        private final Set<Id> ids;
        private final InvalidateReason reason;

        public Invalidate(Node node, Set<Id> ids, InvalidateReason reason)
        {
            this.node = node;
            this.ids = ImmutableSet.copyOf(ids);
            this.reason = reason;
        }
    }

    public final class BuildInput
            extends JournalEntry
    {
        private final Node node;
        private final Key key;

        public BuildInput(Node node, Key key)
        {
            this.node = node;
            this.key = key;
        }
    }

    public final class BuildOutput
            extends JournalEntry
    {

    }

    public final class StateCachedBuildOutput
            extends JournalEntry
    {

    }

    public final class RowCachedBuildOutput
            extends JournalEntry
    {

    }

    public final class UncachedBuildOutput
            extends JournalEntry
    {

    }

    public final class ScanBuildOutput
            extends JournalEntry
    {

    }

    public final class DenormalizedInputJournalEntry
            extends JournalEntry
    {

    }
}
