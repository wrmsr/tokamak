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

import com.wrmsr.tokamak.core.driver.DriverRow;
import com.wrmsr.tokamak.core.plan.node.PState;

import java.util.Iterator;
import java.util.Set;

public enum LineageRetention
{
    NOP() {
        @Override
        void consume(Iterator<DriverRow> rows, Sink sink)
        {
        }
    },

    MINIMAL() {
        @Override
        void consume(Iterator<DriverRow> rows, Sink sink)
        {
            while (rows.hasNext()) {
                DriverRow row = rows.next();
                if (row.getNode() instanceof PState) {
                    sink.accept(row);
                }
                else {
                    sink.accept(row.getLineage());
                }
            }
        }
    },

    CASCADE() {
        @Override
        void consume(Iterator<DriverRow> rows, Sink sink)
        {
            while (rows.hasNext()) {
                DriverRow row = rows.next();
                if (row.getNode() instanceof PState) {
                    sink.accept(row);
                }
                else {
                    sink.accept(row.getLineage());
                    sink.accept(row);
                }
            }
        }
    },

    FULL() {
        @Override
        void consume(Iterator<DriverRow> rows, Sink sink)
        {
            while (rows.hasNext()) {
                DriverRow row = rows.next();
                sink.accept(row.getLineage());
                sink.accept(row);
            }
        }
    },

    ;

    interface Sink
    {
        void accept(DriverRow row);

        void accept(Set<LineageEntry> lineage);
    }

    abstract void consume(Iterator<DriverRow> rows, Sink sink);
}
