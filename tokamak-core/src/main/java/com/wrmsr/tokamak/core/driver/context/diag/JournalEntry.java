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
import com.wrmsr.tokamak.core.driver.build.ops.BuildOp;
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.Arrays;
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
        private final PNode node;
        private final Set<Id> ids;
        private final InvalidateReason reason;

        public Invalidate(PNode node, Set<Id> ids, InvalidateReason reason)
        {
            this.node = checkNotNull(node);
            this.ids = ImmutableSet.copyOf(ids);
            this.reason = checkNotNull(reason);
        }

        @Override
        public String toString()
        {
            return "Invalidate{" +
                    "node=" + node +
                    ", ids=" + ids +
                    ", reason=" + reason +
                    '}';
        }
    }

    public static final class BuildInput
            extends JournalEntry
    {
        private final PNode node;
        private final Key key;

        public BuildInput(PNode node, Key key)
        {
            this.node = checkNotNull(node);
            this.key = checkNotNull(key);
        }

        @Override
        public String toString()
        {
            return "BuildInput{" +
                    "node=" + node +
                    ", key=" + key +
                    '}';
        }
    }

    public static final class BuildOutput
            extends JournalEntry
    {
        private final PNode node;
        private final Key key;
        private final Collection<DriverRow> rows;

        public BuildOutput(PNode node, Key key, Collection<DriverRow> rows)
        {
            this.node = checkNotNull(node);
            this.key = checkNotNull(key);
            this.rows = checkNotNull(rows);
        }

        @Override
        public String toString()
        {
            return "BuildOutput{" +
                    "node=" + node +
                    ", key=" + key +
                    ", rows=" + rows +
                    '}';
        }
    }

    public static final class ContextBuildOp
            extends JournalEntry
    {
        private final BuildOp op;

        public ContextBuildOp(BuildOp op)
        {
            this.op = checkNotNull(op);
        }

        @Override
        public String toString()
        {
            return "ContextBuildOp{" +
                    "op=" + op +
                    '}';
        }
    }

    public static final class ContextBuildOpCallback
            extends JournalEntry
    {
        private final BuildOp op;
        private final Object[] args;

        public ContextBuildOpCallback(BuildOp op, Object... args)
        {
            this.op = checkNotNull(op);
            this.args = checkNotNull(args);
        }

        @Override
        public String toString()
        {
            return "ContextBuildOpCallback{" +
                    "op=" + op +
                    ", args=" + Arrays.toString(args) +
                    '}';
        }
    }
}
