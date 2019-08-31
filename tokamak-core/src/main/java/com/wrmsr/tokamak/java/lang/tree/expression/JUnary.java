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
package com.wrmsr.tokamak.java.lang.tree.expression;

import com.wrmsr.tokamak.java.lang.op.JUnaryOp;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JUnary
        extends JExpression
{
    private final JUnaryOp op;
    private final JExpression value;

    public JUnary(JUnaryOp op, JExpression value)
    {
        this.op = checkNotNull(op);
        this.value = checkNotNull(value);
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
        JUnary jUnary = (JUnary) o;
        return op == jUnary.op &&
                Objects.equals(value, jUnary.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(op, value);
    }

    public JUnaryOp getOp()
    {
        return op;
    }

    public JExpression getValue()
    {
        return value;
    }

    @Override
    public <R, C> R accept(JExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitJUnary(this, context);
    }
}
