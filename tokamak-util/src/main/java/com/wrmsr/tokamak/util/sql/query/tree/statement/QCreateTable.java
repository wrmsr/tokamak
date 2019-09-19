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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.sql.query.QName;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class QCreateTable
        extends QStatement
{
    @Immutable
    public static final class Column
    {
        private final String name;
        private final String type;

        public Column(String name, String type)
        {
            this.name = checkNotNull(name);
            this.type = checkNotNull(type);
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }
    }

    private final QName name;
    private final List<Column> columns;

    public QCreateTable(QName name, List<Column> columns)
    {
        this.name = checkNotNull(name);
        this.columns = ImmutableList.copyOf(columns);
    }

    public QName getName()
    {
        return name;
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    @Override
    public <R, C> R accept(QStatementVisitor<R, C> visitor, C context)
    {
        return visitor.visitQCreateTable(this, context);
    }
}
