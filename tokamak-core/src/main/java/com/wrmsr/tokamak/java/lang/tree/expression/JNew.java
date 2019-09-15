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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.java.lang.JTypeSpecifier;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JNew
        extends JExpression
{
    private final JTypeSpecifier type;
    private final List<JExpression> args;

    public JNew(JTypeSpecifier type, List<JExpression> args)
    {
        this.type = checkNotNull(type);
        this.args = ImmutableList.copyOf(args);
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
        JNew that = (JNew) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(args, that.args);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, args);
    }

    public JTypeSpecifier getType()
    {
        return type;
    }

    public List<JExpression> getArgs()
    {
        return args;
    }

    @Override
    public <R, C> R accept(JExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitJNew(this, context);
    }
}
