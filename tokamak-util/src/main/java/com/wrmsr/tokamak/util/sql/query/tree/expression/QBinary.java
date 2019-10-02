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
package com.wrmsr.tokamak.util.sql.query.tree.expression;

import com.wrmsr.tokamak.util.sql.query.tree.op.QBinaryOp;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class QBinary
        extends QExpression
{
    private final QExpression left;
    private final QBinaryOp op;
    private final QExpression right;

    public QBinary(QExpression left, QBinaryOp op, QExpression right)
    {
        this.left = checkNotNull(left);
        this.op = checkNotNull(op);
        this.right = checkNotNull(right);
    }

    public QExpression getLeft()
    {
        return left;
    }

    public QBinaryOp getOp()
    {
        return op;
    }

    public QExpression getRight()
    {
        return right;
    }

    @Override
    public <R, C> R accept(QExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitBinary(this, context);
    }
}
