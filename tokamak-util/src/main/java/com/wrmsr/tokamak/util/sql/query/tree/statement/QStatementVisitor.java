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
package com.wrmsr.tokamak.util.sql.query.tree.statement;

import java.util.Objects;

public class QStatementVisitor<R, C>
{
    protected R process(QStatement qstatement, C context)
    {
        return qstatement.accept(this, context);
    }

    protected R visitStatement(QStatement qstatement, C context)
    {
        throw new IllegalStateException(Objects.toString(qstatement));
    }

    public R visitCreateTable(QCreateTable qstatement, C context)
    {
        return visitStatement(qstatement, context);
    }

    public R visitDelete(QDelete qstatement, C context)
    {
        return visitStatement(qstatement, context);
    }

    public R visitDropTable(QDropTable qstatement, C context)
    {
        return visitStatement(qstatement, context);
    }

    public R visitInsert(QInsert qstatement, C context)
    {
        return visitStatement(qstatement, context);
    }

    public R visitSelect(QSelect qstatement, C context)
    {
        return visitStatement(qstatement, context);
    }

    public R visitUpdate(QUpdate qstatement, C context)
    {
        return visitStatement(qstatement, context);
    }
}
