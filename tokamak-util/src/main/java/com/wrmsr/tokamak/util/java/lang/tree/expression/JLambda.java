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

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.java.lang.tree.statement.JStatement;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JLambda
        extends JExpression
{
    private final List<String> params;
    private final JStatement body;

    public JLambda(List<String> params, JStatement body)
    {
        this.params = ImmutableList.copyOf(params);
        this.body = checkNotNull(body);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        JLambda jLambda = (JLambda) o;
        return Objects.equals(params, jLambda.params) &&
                Objects.equals(body, jLambda.body);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(params, body);
    }

    public List<String> getParams()
    {
        return params;
    }

    public JStatement getBody()
    {
        return body;
    }

    @Override
    public <R, C> R accept(JExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitLambda(this, context);
    }
}
