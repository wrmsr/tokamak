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
package com.wrmsr.tokamak.sql.dsl.ele;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.sql.dsl.ele.sel.FromClause;

import java.util.List;

public class TableClause
        extends FromClause
{
    private final String name;
    private final List<ColumnClause> columns;

    public TableClause(
            String name,
            List<ColumnClause> columns)
    {
        this.name = name;
        this.columns = ImmutableList.copyOf(columns);
        // primaryKey, foreignKeys
    }
}
