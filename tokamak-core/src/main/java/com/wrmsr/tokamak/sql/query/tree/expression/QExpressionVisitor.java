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
package com.wrmsr.tokamak.sql.query.tree.expression;

import java.util.Objects;

public class QExpressionVisitor<R, C>
{
    protected R visitQExpression(QExpression qexpression, C context)
    {
        throw new IllegalStateException(Objects.toString(qexpression));
    }

    public R visitQBinary(QBinary qexpression, C context)
    {
        return visitQExpression(qexpression, context);
    }

    public R visitQParen(QParen qexpression, C context)
    {
        return visitQExpression(qexpression, context);
    }

    public R visitQReferenceExpression(QReferenceExpression qexpression, C context)
    {
        return visitQExpression(qexpression, context);
    }

    public R visitQSubqueryExpression(QSubqueryExpression qexpression, C context)
    {
        return visitQExpression(qexpression, context);
    }

    public R visitQTextExpression(QTextExpression qexpression, C context)
    {
        return visitQExpression(qexpression, context);
    }

    public R visitQUnary(QUnary qexpression, C context)
    {
        return visitQExpression(qexpression, context);
    }
}
