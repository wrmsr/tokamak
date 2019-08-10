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
package com.wrmsr.tokamak.sql;

import java.util.Map;
import java.util.stream.StreamSupport;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public final class Table
{
    private final String name;
    private final Map<String, Column> columnsByName;

    public Table(String name, Iterable<Column> columns)
    {
        this.name = name;
        this.columnsByName = StreamSupport.stream(columns.spliterator(), false).collect(toImmutableMap(Column::getName, identity()));
    }

    public String getName()
    {
        return name;
    }

    public Map<String, Column> getColumnsByName()
    {
        return columnsByName;
    }
}
