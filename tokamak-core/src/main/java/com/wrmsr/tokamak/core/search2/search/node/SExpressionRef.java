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
package com.wrmsr.tokamak.core.search2.search.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.core.search2.search.node.visitor.SNodeVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SExpressionRef
        extends SNode
{
    private final SNode expression;

    @JsonCreator
    public SExpressionRef(
            @JsonProperty("expression") SNode expression)
    {
        this.expression = checkNotNull(expression);
    }

    @JsonProperty("expression")
    public SNode getExpression()
    {
        return expression;
    }

    @Override
    public <R, C> R accept(SNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitExpressionRef(this, context);
    }
}
