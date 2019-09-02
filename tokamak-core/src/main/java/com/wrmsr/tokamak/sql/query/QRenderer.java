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
package com.wrmsr.tokamak.sql.query;

import com.google.common.base.Joiner;
import com.wrmsr.tokamak.sql.query.tree.expression.QBinary;
import com.wrmsr.tokamak.sql.query.tree.expression.QExpression;
import com.wrmsr.tokamak.sql.query.tree.expression.QExpressionVisitor;
import com.wrmsr.tokamak.sql.query.tree.expression.QParen;
import com.wrmsr.tokamak.sql.query.tree.expression.QTextExpression;
import com.wrmsr.tokamak.sql.query.tree.expression.QUnary;
import com.wrmsr.tokamak.sql.query.tree.relation.QReferenceRelation;
import com.wrmsr.tokamak.sql.query.tree.relation.QRelation;
import com.wrmsr.tokamak.sql.query.tree.relation.QRelationVisitor;
import com.wrmsr.tokamak.sql.query.tree.statement.QCreateTable;
import com.wrmsr.tokamak.sql.query.tree.statement.QSelect;
import com.wrmsr.tokamak.sql.query.tree.statement.QStatement;
import com.wrmsr.tokamak.sql.query.tree.statement.QStatementVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

public class QRenderer
{
    private final StringBuilder sb;

    public QRenderer(StringBuilder sb)
    {
        this.sb = checkNotNull(sb);
    }

    public String quoteName(String name)
    {
        return '"' + name + '"';
    }

    public void renderName(QName name)
    {
        sb.append(Joiner.on('.').join(name.getParts().stream().map(this::quoteName).iterator()));
    }

    public void renderStatement(QStatement statement)
    {
        statement.accept(new QStatementVisitor<Void, Void>()
        {
            @Override
            public Void visitQCreateTable(QCreateTable qstatement, Void context)
            {
                sb.append("CREATE TABLE ");
                renderName(qstatement.getName());
                sb.append(" (");
                int i = 0;
                for (QCreateTable.Column col : qstatement.getColumns()) {
                    if (i++ > 0) {
                        sb.append(", ");
                    }
                    sb.append(quoteName(col.getName()));
                    sb.append(" ");
                    sb.append(col.getType());
                }
                sb.append(")");
                return null;
            }

            @Override
            public Void visitQSelect(QSelect qstatement, Void context)
            {
                sb.append("SELECT");
                int i = 0;
                for (QSelect.Item item : qstatement.getItems()) {
                    if (i++ > 0) {
                        sb.append(",");
                    }
                    sb.append(" ");
                    renderExpression(item.getExpression());
                    item.getLabel().ifPresent(l -> sb.append(String.format("AS %s", l)));
                }
                qstatement.getRelation().ifPresent(r -> {
                    sb.append(" FROM ");
                    renderRelation(r);
                });
                qstatement.getWhere().ifPresent(w -> {
                    sb.append(" WHERE ");
                    renderExpression(w);
                });
                return null;
            }
        }, null);
    }

    public void renderRelation(QRelation relation)
    {
        relation.accept(new QRelationVisitor<Void, Void>()
        {
            @Override
            public Void visitQReferenceRelation(QReferenceRelation qrelation, Void context)
            {
                renderName(qrelation.getName());
                return null;
            }
        }, null);
    }

    public void renderExpression(QExpression expression)
    {
        expression.accept(new QExpressionVisitor<Void, Void>()
        {
            @Override
            public Void visitQBinary(QBinary qexpression, Void context)
            {
                renderExpression(qexpression.getLeft());
                sb.append(" ");
                if (qexpression.getOp().isRequiresSpace()) {
                    sb.append(" ");
                }
                sb.append(qexpression.getOp().getAnsi().get());
                if (qexpression.getOp().isRequiresSpace()) {
                    sb.append(" ");
                }
                sb.append(" ");
                renderExpression(qexpression.getRight());
                return null;
            }

            @Override
            public Void visitQParen(QParen qexpression, Void context)
            {
                sb.append("(");
                renderExpression(qexpression.getChild());
                sb.append(")");
                return null;
            }

            @Override
            public Void visitQTextExpression(QTextExpression qexpression, Void context)
            {
                sb.append(qexpression.getText());
                return null;
            }

            @Override
            public Void visitQUnary(QUnary qexpression, Void context)
            {
                sb.append(qexpression.getOp().getAnsi().get());
                if (qexpression.getOp().isRequiresSpace()) {
                    sb.append(" ");
                }
                renderExpression(qexpression.getChild());
                return null;
            }
        }, null);
    }

    public static String renderStatementString(QStatement statement)
    {
        StringBuilder sb = new StringBuilder();
        new QRenderer(sb).renderStatement(statement);
        return sb.toString();
    }
}
