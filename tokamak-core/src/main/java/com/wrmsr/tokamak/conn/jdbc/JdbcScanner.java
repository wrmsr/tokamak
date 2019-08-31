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
package com.wrmsr.tokamak.conn.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.AllKey;
import com.wrmsr.tokamak.api.FieldKey;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.catalog.Connection;
import com.wrmsr.tokamak.catalog.Scanner;
import com.wrmsr.tokamak.layout.RowLayout;
import com.wrmsr.tokamak.layout.TableLayout;
import com.wrmsr.tokamak.sql.SqlConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.sql.SqlUtils.execute;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static java.util.function.Function.identity;

public final class JdbcScanner
        implements Scanner
{
    private final SchemaTable schemaTable;
    private final TableLayout tableLayout;
    private final Set<String> fields;

    private final RowLayout rowLayout;
    private final Set<String> idFields;

    private final Map<Set<String>, Instance> instancesByKeyFieldSets = new ConcurrentHashMap<>();

    public JdbcScanner(
            SchemaTable schemaTable,
            TableLayout tableLayout,
            Set<String> fields)
    {
        this.schemaTable = schemaTable;
        this.tableLayout = tableLayout;
        this.fields = ImmutableSet.copyOf(checkOrdered(fields));

        for (String field : this.fields) {
            checkArgument(tableLayout.getRowLayout().getFields().containsKey(field));
        }

        rowLayout = new RowLayout(
                fields.stream()
                        .collect(toImmutableMap(identity(), tableLayout.getRowLayout().getFields()::get)));

        idFields = ImmutableSet.copyOf(tableLayout.getPrimaryKey().getFields());
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

    private final class Instance
    {
        private final Set<String> keyFields;

        private final Set<String> selectedFields;
        private final String stmt;

        private Instance(Set<String> keyFields)
        {
            this.keyFields = ImmutableSet.copyOf(checkOrdered(keyFields));
            this.keyFields.forEach(f -> checkArgument(tableLayout.getRowLayout().getFields().containsKey(f)));

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
                        Joiner.on(" and ").join(this.keyFields.stream().map(f -> String.format("%s = ?", f, f)).collect(toImmutableList()));
            }

            this.stmt = stmt;
        }

        public List<Map<String, Object>> getRows(SqlConnection conn, Map<String, Object> keyValuesByField)
        {
            checkArgument(keyValuesByField.keySet().equals(keyFields));

            Object[] args = new Object[keyFields.size()];
            int i = 0;
            for (String kf : keyFields) {
                args[i++] = keyValuesByField.get(kf);
            }

            try {
                return execute(conn.getConnection(), stmt, args);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Instance getInstance(Set<String> keyFields)
    {
        return instancesByKeyFieldSets.computeIfAbsent(ImmutableSet.copyOf(keyFields), Instance::new);
    }

    @Override
    public List<Map<String, Object>> scan(Connection connection, Key key)
    {
        JdbcConnection jdbcConnection = (JdbcConnection) connection;
        SqlConnection sqlConnection = jdbcConnection.getSqlConnection();

        if (key instanceof FieldKey) {
            FieldKey fieldKey = (FieldKey) key;
            Instance instance = getInstance(fieldKey.getValuesByField().keySet());
            return instance.getRows(sqlConnection, fieldKey.getValuesByField());
        }
        else if (key instanceof AllKey) {
            Instance instance = getInstance(ImmutableSet.of());
            return instance.getRows(sqlConnection, ImmutableMap.of());
        }
        else {
            throw new IllegalArgumentException(Objects.toString(key));
        }
    }
}
