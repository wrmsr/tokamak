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
package com.wrmsr.tokamak.core.conn.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.catalog.Connection;
import com.wrmsr.tokamak.core.catalog.Scanner;
import com.wrmsr.tokamak.core.layout.RowLayout;
import com.wrmsr.tokamak.core.layout.TableLayout;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.util.sql.SqlConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapItems;
import static com.wrmsr.tokamak.util.sql.SqlUtils.execute;
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
            checkArgument(tableLayout.getRowLayout().getFields().contains(field));
        }

        rowLayout = new RowLayout(FieldCollection.of(
                fields.stream()
                        .collect(toImmutableMap(identity(), tableLayout.getRowLayout().getFields().getTypesByName()::get))));

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
            this.keyFields.forEach(f -> checkArgument(tableLayout.getRowLayout().getFields().contains(f)));

            selectedFields = ImmutableSet.<String>builder()
                    .addAll(this.keyFields)
                    .addAll(idFields)
                    .addAll(fields)
                    .build();

            String stmt = "" +
                    "select " +
                    Joiner.on(", ").join(ImmutableList.copyOf(selectedFields)) +
                    " from " +
                    schemaTable.getSchema() + "." + schemaTable.getTable();

            if (!this.keyFields.isEmpty()) {
                stmt += "" +
                        " where " +
                        Joiner.on(" and ").join(immutableMapItems(this.keyFields, f -> String.format("%s = ?", f)));
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
        Instance instance = getInstance(key.getValuesByField().keySet());
        return instance.getRows(sqlConnection, key.getValuesByField());
    }
}
