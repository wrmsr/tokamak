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

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JMemberAccess
        extends JExpression
{
    private final JExpression instance;
    private final String member;

    public JMemberAccess(JExpression instance, String member)
    {
        this.instance = checkNotNull(instance);
        this.member = checkNotNull(member);
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
        JMemberAccess that = (JMemberAccess) o;
        return Objects.equals(instance, that.instance) &&
                Objects.equals(member, that.member);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(instance, member);
    }

    public JExpression getInstance()
    {
        return instance;
    }

    public String getMember()
    {
        return member;
    }

    @Override
    public <R, C> R accept(JExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitJMemberAccess(this, context);
    }
}
