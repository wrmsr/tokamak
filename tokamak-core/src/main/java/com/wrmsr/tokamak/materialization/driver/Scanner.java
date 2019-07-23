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
package com.wrmsr.tokamak.materialization.driver;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.materialization.api.Attributes;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.Id;
import com.wrmsr.tokamak.materialization.api.Payload;
import com.wrmsr.tokamak.materialization.api.TableName;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class Scanner
{
    private final TableName table;
    private final Set<FieldName> fields;

    public Scanner(TableName table, Set<FieldName> fields)
    {
        this.table = table;
        this.fields = ImmutableSet.copyOf(fields);
    }

    public List<Payload> scan(Handle handle, FieldName field, int value)
    {
        String stmt = "" +
                "select " +
                Joiner.on(", ").join(fields.stream().map(FieldName::getValue).collect(toImmutableList())) +
                " from " +
                table.getValue() +
                " where " +
                field.getValue() +
                " = :value";

        List<Map<String, Object>> rows = handle
                .createQuery(stmt).bind("value", value)
                .mapToMap()
                .list();

        return rows
                .stream()
                .map(row -> new Payload(
                        Id.of(value),
                        new Attributes(
                                row
                                        .entrySet()
                                        .stream()
                                        .collect(toImmutableMap(e -> FieldName.of(e.getKey()), Map.Entry::getValue)))))
                .collect(toImmutableList());
    }
}
