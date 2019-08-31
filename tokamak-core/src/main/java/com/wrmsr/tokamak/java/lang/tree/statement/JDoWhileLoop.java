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

import static java.util.Objects.requireNonNull;

@Immutable
public final class JDoWhileLoop
        extends JStatement
{
    private final JBlock body;
    private final JExpression condition;

    public JDoWhileLoop(JBlock body, JExpression condition)
    {
        this.body = requireNonNull(body);
        this.condition = requireNonNull(condition);
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
        JDoWhileLoop that = (JDoWhileLoop) o;
        return Objects.equals(body, that.body) &&
                Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(body, condition);
    }

    public JBlock getBody()
    {
        return body;
    }

    public JExpression getCondition()
    {
        return condition;
    }

    @Override
    public <R, C> R accept(JStatementVisitor<R, C> visitor, C context)
    {
        return visitor.visitJDoWhileLoop(this, context);
    }
}
