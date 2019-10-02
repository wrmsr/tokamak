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
package com.wrmsr.tokamak.util.java.lang.tree.expression;

import com.wrmsr.tokamak.util.java.lang.op.JBinaryOp;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JBinary
        extends JExpression
{
    private final JBinaryOp op;
    private final JExpression left;
    private final JExpression right;

    public JBinary(JBinaryOp op, JExpression left, JExpression right)
    {
        this.op = checkNotNull(op);
        this.left = checkNotNull(left);
        this.right = checkNotNull(right);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JBinary jBinary = (JBinary) o;
        return op == jBinary.op &&
                Objects.equals(left, jBinary.left) &&
                Objects.equals(right, jBinary.right);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(op, left, right);
    }

    public JBinaryOp getOp()
    {
        return op;
    }

    public JExpression getLeft()
    {
        return left;
    }

    public JExpression getRight()
    {
        return right;
    }

    @Override
    public <R, C> R accept(JExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitBinary(this, context);
    }
}
