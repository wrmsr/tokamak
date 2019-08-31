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

import com.wrmsr.tokamak.java.lang.JName;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class JMethodReference
        extends JExpression
{
    private final JName className;
    private final String methodName;

    public JMethodReference(JName className, String methodName)
    {
        this.className = checkNotNull(className);
        this.methodName = checkNotNull(methodName);
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
        JMethodReference that = (JMethodReference) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(className, methodName);
    }

    public JName getClassName()
    {
        return className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    @Override
    public <R, C> R accept(JExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitJMethodReference(this, context);
    }
}