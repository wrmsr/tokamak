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

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JLabeledStatement
        extends JStatement
{
    private final String label;
    private final JStatement statement;

    public JLabeledStatement(String label, JStatement statement)
    {
        this.label = checkNotNull(label);
        this.statement = checkNotNull(statement);
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
        JLabeledStatement that = (JLabeledStatement) o;
        return Objects.equals(label, that.label) &&
                Objects.equals(statement, that.statement);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(label, statement);
    }

    public String getLabel()
    {
        return label;
    }

    public JStatement getStatement()
    {
        return statement;
    }

    @Override
    public <R, C> R accept(JStatementVisitor<R, C> visitor, C context)
    {
        return visitor.visitJLabeledStatement(this, context);
    }
}
