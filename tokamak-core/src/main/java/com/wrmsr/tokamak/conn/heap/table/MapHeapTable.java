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
package com.wrmsr.tokamak.conn.heap.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.layout.TableLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MapHeapTable
        implements HeapTable
{
    /*
    TODO:
     - guava table
     - key sharing
     - json Object coercion from tableLayout
    */

    private final SchemaTable schemaTable;
    private final TableLayout tableLayout;
    private final List<Object[]> rows = new ArrayList<>();

    @JsonCreator
    public MapHeapTable(
            @JsonProperty("schemaTable") SchemaTable schemaTable,
            @JsonProperty("tableLayout") TableLayout tableLayout,
            @JsonProperty("rows") List<Object[]> rows)
    {
        this.schemaTable = checkNotNull(schemaTable);
        this.tableLayout = checkNotNull(tableLayout);
        this.rows.addAll(rows);
    }

    public MapHeapTable(SchemaTable schemaTable, TableLayout tableLayout)
    {
        this(schemaTable, tableLayout, ImmutableList.of());
    }

    @JsonProperty("schemaTable")
    @Override
    public SchemaTable getSchemaTable()
    {
        return schemaTable;
    }

    @JsonProperty("tableLayout")
    @Override
    public TableLayout getTableLayout()
    {
        return tableLayout;
    }

    @JsonProperty("rows")
    public List<Object[]> getRows()
    {
        return rows;
    }

    public MapHeapTable addRowArrays(Iterable<Object[]> rows)
    {
        rows.forEach(row -> {
            checkNotNull(row);
            checkArgument(row.length == tableLayout.getRowLayout().getFields().size());
            this.rows.add(row);
        });
        return this;
    }

    public MapHeapTable addRowLists(Iterable<List<Object>> rows)
    {
        return addRowArrays(Iterables.transform(rows, List::toArray));
    }

    public MapHeapTable addRowMaps(Iterable<Map<String, Object>> rows, boolean strict)
    {
        int length = tableLayout.getRowLayout().getFields().size();
        return addRowArrays(Iterables.transform(rows, r -> getTableLayout().getRowLayout().mapToArray(r, strict)));
    }

    public MapHeapTable addRowMaps(Iterable<Map<String, Object>> rows)
    {
        return addRowMaps(rows, true);
    }

    @Override
    public List<Map<String, Object>> scan(Set<String> fields, Key key)
    {
        checkArgument(tableLayout.getRowLayout().getFields().keySet().containsAll(fields));

        if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            checkArgument(tableLayout.getRowLayout().getFields().keySet().containsAll(fields));
        }
        else if (key instanceof AllKey) {
            // pass
        }
        else {
            throw new IllegalArgumentException();
        }

        ImmutableList.Builder<Map<String, Object>> builder = ImmutableList.builder();

        rowLoop:
        for (Object[] row : rows) {
            if (key instanceof FieldKey) {
                FieldKey fieldKey = (FieldKey) key;
                for (Map.Entry<String, Object> keyEntry : fieldKey.getValuesByField().entrySet()) {
                    if (!Objects.equals(row[tableLayout.getRowLayout().getPositionsByField().get(keyEntry.getKey())], ((FieldKey) key).get(keyEntry.getKey()))) {
                        continue rowLoop;
                    }
                }
            }
            builder.add(tableLayout.getRowLayout().arrayToMap(row));
        }

        return builder.build();
    }
}
