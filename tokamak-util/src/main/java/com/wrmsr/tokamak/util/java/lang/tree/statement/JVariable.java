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

import com.wrmsr.tokamak.util.java.lang.JTypeSpecifier;
import com.wrmsr.tokamak.util.java.lang.tree.expression.JExpression;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JVariable
        extends JStatement
{
    private final JTypeSpecifier type;
    private final String name;
    private final Optional<JExpression> value;

    public JVariable(JTypeSpecifier type, String name, Optional<JExpression> value)
    {
        this.type = checkNotNull(type);
        this.name = checkNotNull(name);
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
        JVariable that = (JVariable) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, name, value);
    }

    public JTypeSpecifier getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public Optional<JExpression> getValue()
    {
        return value;
    }

    @Override
    public <R, C> R accept(JStatementVisitor<R, C> visitor, C context)
    {
        return visitor.visitVariable(this, context);
    }
}
