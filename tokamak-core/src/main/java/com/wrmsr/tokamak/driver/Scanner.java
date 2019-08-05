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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.api.SimpleRow;
import com.wrmsr.tokamak.codec.IdCodecs;
import com.wrmsr.tokamak.codec.RowIdCodec;
import com.wrmsr.tokamak.layout.RowLayout;
import com.wrmsr.tokamak.layout.TableLayout;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.mapper.MapMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class Scanner
{
    private final String table;
    private final TableLayout tableLayout;
    private final Set<String> fields;

    private final RowLayout rowLayout;
    private final RowIdCodec rowIdCodec;

    private final Map<String, Instance> instancesByField = new ConcurrentHashMap<>();

    public Scanner(String table, TableLayout tableLayout, Set<String> fields)
    {
        this.table = table;
        this.tableLayout = tableLayout;
        this.fields = ImmutableSet.copyOf(fields);

        for (String field : this.fields) {
            checkArgument(tableLayout.getRowLayout().getFields().contains(field));
        }

        rowLayout = new RowLayout(
                fields.stream()
                        .collect(toImmutableMap(identity(), tableLayout.getRowLayout().getTypesByField()::get)));

        rowIdCodec = IdCodecs.buildRowIdCodec(
                tableLayout.getPrimaryKey().stream()
                        .collect(toImmutableMap(identity(), tableLayout.getRowLayout().getTypesByField()::get)));
    }

    public String getTable()
    {
        return table;
    }

    public TableLayout getTableLayout()
    {
        return tableLayout;
    }

    public Set<String> getFields()
    {
        return fields;
    }

    public RowLayout getRowLayout()
    {
        return rowLayout;
    }

    public RowIdCodec getRowIdCodec()
    {
        return rowIdCodec;
    }

    private final class Instance
    {
        private final Set<String> selectedFields;
        private final String stmt;

        private Instance(String keyField)
        {
            checkArgument(tableLayout.getRowLayout().getFields().contains(keyField));

            ImmutableSet.Builder<String> selectedFields = ImmutableSet.builder();
            if (!fields.contains(keyField)) {
                selectedFields.add(keyField);
            }
            for (String field : tableLayout.getPrimaryKey().getFields()) {
                if (!fields.contains(field)) {
                    selectedFields.add(field);
                }
            }
            selectedFields.addAll(fields);
            this.selectedFields = selectedFields.build();

            stmt = "" +
                    "select " +
                    Joiner.on(", ").join(this.selectedFields.stream().collect(toImmutableList())) +
                    " from " +
                    table +
                    " where " +
                    keyField +
                    " = :value";
        }

        public List<SimpleRow> getRows(Handle handle, Object value)
        {
            List<Map<String, Object>> rawRows = handle
                    .createQuery(stmt).bind("value", value)
                    .map(new MapMapper(false))
                    .list();

            ImmutableList.Builder<SimpleRow> rows = ImmutableList.builder();
            for (Map<String, Object> rawRow : rawRows) {
                Id id = Id.of(rowIdCodec.encode(rawRow));
                Object[] attributes = rawRow.entrySet().stream()
                        .filter(e -> fields.contains(e.getKey()))
                        .map(Map.Entry::getValue)
                        .toArray(Object[]::new);
                rows.add(new SimpleRow(id, attributes));
            }
            return rows.build();
        }
    }

    private Instance getInstance(String keyField)
    {
        return instancesByField.computeIfAbsent(keyField, Instance::new);
    }

    public List<SimpleRow> scan(Handle handle, Key key)
    {
        if (!(key instanceof FieldKey)) {
            throw new IllegalArgumentException();
        }
        return getInstance(((FieldKey) key).getField()).getRows(handle, ((FieldKey) key).getValue());
    }
}
