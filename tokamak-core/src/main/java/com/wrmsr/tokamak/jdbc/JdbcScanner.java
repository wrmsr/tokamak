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
package com.wrmsr.tokamak.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.api.SimpleRow;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.codec.IdCodecs;
import com.wrmsr.tokamak.codec.RowIdCodec;
import com.wrmsr.tokamak.layout.RowLayout;
import com.wrmsr.tokamak.layout.TableLayout;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.mapper.MapMapper;
import org.jdbi.v3.core.statement.Query;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public final class JdbcScanner
        implements Scanner
{
    private final SchemaTable schemaTable;
    private final TableLayout tableLayout;
    private final Set<String> fields;

    private final RowLayout rowLayout;
    private final Set<String> idFields;
    private final RowIdCodec rowIdCodec;

    private final Map<Set<String>, Instance> instancesByKeyFieldSets = new ConcurrentHashMap<>();

    public JdbcScanner(
            SchemaTable schemaTable,
            TableLayout tableLayout,
            Set<String> fields)
    {
        this.schemaTable = schemaTable;
        this.tableLayout = tableLayout;
        this.fields = ImmutableSet.copyOf(fields);

        for (String field : this.fields) {
            checkArgument(tableLayout.getRowLayout().getFields().contains(field));
        }

        rowLayout = new RowLayout(
                fields.stream()
                        .collect(toImmutableMap(identity(), tableLayout.getRowLayout().getTypesByField()::get)));

        idFields = ImmutableSet.copyOf(tableLayout.getPrimaryKey().getFields());

        rowIdCodec = IdCodecs.buildRowIdCodec(
                idFields.stream()
                        .collect(toImmutableMap(identity(), tableLayout.getRowLayout().getTypesByField()::get)));
    }

    public SchemaTable getSchemaTable()
    {
        return schemaTable;
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
        private final Set<String> keyFields;

        private final boolean keyHasIdFields;
        private final Set<String> selectedFields;
        private final String stmt;

        private Instance(Set<String> keyFields)
        {
            this.keyFields = ImmutableSet.copyOf(keyFields);
            this.keyFields.forEach(f -> checkArgument(tableLayout.getRowLayout().getFields().contains(f)));

            keyHasIdFields = Sets.difference(idFields, keyFields).isEmpty();

            selectedFields = ImmutableSet.<String>builder()
                    .addAll(this.keyFields)
                    .addAll(idFields)
                    .addAll(fields)
                    .build();

            String stmt = "" +
                    "select " +
                    Joiner.on(", ").join(selectedFields.stream().collect(toImmutableList())) +
                    " from " +
                    schemaTable.getSchema() + "." + schemaTable.getTable();

            if (!this.keyFields.isEmpty()) {
                stmt += "" +
                        " where " +
                        Joiner.on(" and ").join(this.keyFields.stream().map(f -> String.format("%s = :%s", f, f)).collect(toImmutableList()));
            }

            this.stmt = stmt;
        }

        public List<Row> getRows(Handle handle, Map<String, Object> keyValuesByField)
        {
            checkArgument(keyValuesByField.keySet().equals(keyFields));

            Query query = handle.createQuery(stmt);
            for (Map.Entry<String, Object> e : keyValuesByField.entrySet()) {
                query = query.bind(e.getKey(), e.getValue());
            }
            List<Map<String, Object>> rawRows = query
                    .map(new MapMapper(false))
                    .list();

            if (rawRows.isEmpty()) {
                Id id;
                if (keyHasIdFields) {
                    id = Id.of(rowIdCodec.encode(keyValuesByField));
                }
                else {
                    id = null;
                }
                return ImmutableList.of(
                        new SimpleRow(
                                id,
                                null));
            }

            ImmutableList.Builder<Row> rows = ImmutableList.builder();
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

    private Instance getInstance(Set<String> keyFields)
    {
        return instancesByKeyFieldSets.computeIfAbsent(keyFields, Instance::new);
    }

    @Override
    public List<Map<String, Object>> scan(Connection connection, Key key)
    {
        JdbcConnection jdbcConnection = (JdbcConnection) connection;
        Handle handle = jdbcConnection.getHandle();

        if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Instance instance = getInstance(fieldKey.getValuesByField().keySet());
            return instance.getRows(handle, fieldKey.getValuesByField());
        }
        else if (key instanceof AllKey) {
            Instance instance = getInstance(ImmutableSet.of());
            return instance.getRows(handle, ImmutableMap.of());
        }
        else {
            throw new IllegalArgumentException(key.toString());
        }
    }
}
