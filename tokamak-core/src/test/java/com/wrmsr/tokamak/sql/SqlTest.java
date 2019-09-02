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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.sql.query.QName;
import com.wrmsr.tokamak.sql.query.QRenderer;
import com.wrmsr.tokamak.sql.query.tree.expression.QTextExpression;
import com.wrmsr.tokamak.sql.query.tree.relation.QReferenceRelation;
import com.wrmsr.tokamak.sql.query.tree.statement.QCreateTable;
import com.wrmsr.tokamak.sql.query.tree.statement.QSelect;
import com.wrmsr.tokamak.sql.query.tree.statement.QStatement;
import junit.framework.TestCase;

import java.util.Optional;

public class SqlTest
        extends TestCase
{
    public void testSql()
            throws Throwable
    {
        QStatement stmt = new QCreateTable(
                QName.of("stuff"),
                ImmutableList.of(
                        new QCreateTable.Column("a", "integer primary key"),
                        new QCreateTable.Column("b", "varchar(32)")
                ));

        String src = QRenderer.renderStatementString(stmt);
        System.out.println(src);

        stmt = new QSelect(
                ImmutableList.of(
                        new QSelect.Item(
                                new QTextExpression("*"),
                                Optional.empty())
                ),
                Optional.of(
                        new QReferenceRelation(
                                QName.of("tpch", "stuff"))),
                Optional.empty());

        src = QRenderer.renderStatementString(stmt);
        System.out.println(src);
    }
}
