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

import com.wrmsr.tokamak.sql.dsl.ele.sel.Selectable;
import com.wrmsr.tokamak.sql.dsl.TypeEngine;

import javax.annotation.Nullable;

public class ColumnClause
        extends ColumnElement
{
    private final String text;
    private final @Nullable Selectable table;
    private final @Nullable TypeEngine type;
    private final boolean isLiteral;

    public ColumnClause(
            String text,
            @Nullable Selectable table,
            @Nullable TypeEngine type,
            boolean isLiteral)
    {
        this.text = text;
        this.table = table;
        this.type = type;
        this.isLiteral = isLiteral;
    }
}