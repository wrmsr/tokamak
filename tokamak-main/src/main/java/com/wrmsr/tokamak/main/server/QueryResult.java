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
package com.wrmsr.tokamak.main.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class QueryResult
{
    private final Map<String, Type> columns;
    private final float took;
    private final List<Object[]> rows;

    @JsonCreator
    public QueryResult(
            @JsonProperty("columns") Map<String, Type> columns,
            @JsonProperty("took") float took,
            @JsonProperty("rows") List<Object[]> rows)
    {
        this.columns = ImmutableMap.copyOf(columns);
        this.took = took;
        this.rows = ImmutableList.copyOf(rows);
        this.rows.forEach(r -> checkArgument(r.length == columns.size()));
    }

    @JsonProperty("columns")
    public Map<String, Type> getColumns()
    {
        return columns;
    }

    @JsonProperty("took")
    public float getTook()
    {
        return took;
    }

    @JsonProperty("rows")
    public List<Object[]> getRows()
    {
        return rows;
    }
}
