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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.Row;
import com.wrmsr.tokamak.layout.TableLayout;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

public class Scanner
{
    private final String table;
    private final TableLayout tableLayout;
    private final Set<String> fields;

    public Scanner(String table, TableLayout tableLayout, Set<String> fields)
    {
        this.table = table;
        this.tableLayout = tableLayout;
        this.fields = ImmutableSet.copyOf(fields);

        for (String field : fields) {
            checkArgument(tableLayout.getRowLayout().getFields().contains(field));
        }
    }

    public List<Row> scan(Handle handle, String field, int value)
    {
        String stmt = "" +
                "select " +
                Joiner.on(", ").join(fields.stream().collect(toImmutableList())) +
                " from " +
                table +
                " where " +
                field +
                " = :value";

        List<Map<String, Object>> rows = handle
                .createQuery(stmt).bind("value", value)
                .mapToMap()
                .list();

        return rows
                .stream()
                .map(row -> new Row(
                        Id.of(value),
                        row.values().stream().toArray(Object[]::new)))
                .collect(toImmutableList());
    }
}
