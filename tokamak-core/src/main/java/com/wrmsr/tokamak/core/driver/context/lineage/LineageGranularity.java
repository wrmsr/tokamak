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

import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.plan.node.Node;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public enum LineageGranularity
{
    ID() {
        final class EntryImpl
                implements Lineage.Entry
        {
            private final Node node;
            private final Id id;

            public EntryImpl(Node node, Id id)
            {
                this.node = checkNotNull(node);
                this.id = checkNotNull(id);
            }

            @Override
            public boolean equals(Object o)
            {
                if (this == o) { return true; }
                if (o == null || getClass() != o.getClass()) { return false; }
                EntryImpl entry = (EntryImpl) o;
                return Objects.equals(node, entry.node) &&
                        Objects.equals(id, entry.id);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(node, id);
            }

            @Override
            public String toString()
            {
                return "EntryImpl{" +
                        "node=" + node +
                        ", id=" + id +
                        '}';
            }

            @Override
            public Node getNode()
            {
                return node;
            }

            @Override
            public Id getId()
            {
                return id;
            }
        }

        @Override
        Lineage.Entry newEntry(DriverRow row)
        {
            return new EntryImpl(row.getNode(), row.getId());
        }
    },

    ROW() {
        final class EntryImpl
                implements Lineage.Entry
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
                EntryImpl entry = (EntryImpl) o;
                return Objects.equals(row, entry.row);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(row);
            }

            @Override
            public String toString()
            {
                return "EntryImpl{" +
                        "row=" + row +
                        '}';
            }

            public DriverRow getRow()
            {
                return row;
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

        @Override
        Lineage.Entry newEntry(DriverRow row)
        {
            return new EntryImpl(row);
        }
    },

    ;

    abstract Lineage.Entry newEntry(DriverRow row);
}
