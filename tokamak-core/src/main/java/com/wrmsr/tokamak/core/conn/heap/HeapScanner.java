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
package com.wrmsr.tokamak.core.conn.heap;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.catalog.Connection;
import com.wrmsr.tokamak.core.catalog.Scanner;
import com.wrmsr.tokamak.core.conn.heap.table.HeapTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class HeapScanner
        implements Scanner
{
    private final HeapConnector connector;
    private final HeapTable table;
    private final Set<String> fields;

    public HeapScanner(HeapConnector connector, HeapTable table, Set<String> fields)
    {
        this.connector = checkNotNull(connector);
        this.table = checkNotNull(table);
        this.fields = ImmutableSet.copyOf(fields);
        checkArgument(table.getTableLayout().getRowLayout().getFields().keySet().containsAll(this.fields));
    }

    @Override
    public List<Map<String, Object>> scan(Connection connection, Key key)
    {
        HeapConnection heapConnection = (HeapConnection) checkNotNull(connection);
        checkArgument(heapConnection.getConnector() == connector);
        return table.scan(fields, key);
    }
}
