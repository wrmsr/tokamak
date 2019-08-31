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
package com.wrmsr.tokamak.java.lang.tree.statement;

import com.wrmsr.tokamak.java.lang.tree.expression.JExpression;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JExpressionStatement
        extends JStatement
{
    private final JExpression expression;

    public JExpressionStatement(JExpression expression)
    {
        this.expression = checkNotNull(expression);
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
        JExpressionStatement that = (JExpressionStatement) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(expression);
    }

    public JExpression getExpression()
    {
        return expression;
    }

    @Override
    public <R, C> R accept(JStatementVisitor<R, C> visitor, C context)
    {
        return visitor.visitJExpressionStatement(this, context);
    }
}
