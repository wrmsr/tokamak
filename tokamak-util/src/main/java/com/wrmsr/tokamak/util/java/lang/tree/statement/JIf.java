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
package com.wrmsr.tokamak.util.java.lang.tree.statement;

import com.wrmsr.tokamak.util.java.lang.tree.expression.JExpression;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JIf
        extends JStatement
{
    private final JExpression condition;
    private final JBlock ifTrue;
    private final Optional<JBlock> ifFalse;

    public JIf(
            JExpression condition,
            JBlock ifTrue,
            Optional<JBlock> ifFalse)
    {
        this.condition = checkNotNull(condition);
        this.ifTrue = checkNotNull(ifTrue);
        this.ifFalse = checkNotNull(ifFalse);
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
        JIf jIf = (JIf) o;
        return Objects.equals(condition, jIf.condition) &&
                Objects.equals(ifTrue, jIf.ifTrue) &&
                Objects.equals(ifFalse, jIf.ifFalse);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(condition, ifTrue, ifFalse);
    }

    public JExpression getCondition()
    {
        return condition;
    }

    public JBlock getIfTrue()
    {
        return ifTrue;
    }

    public Optional<JBlock> getIfFalse()
    {
        return ifFalse;
    }

    @Override
    public <R, C> R accept(JStatementVisitor<R, C> visitor, C context)
    {
        return visitor.visitIf(this, context);
    }
}
