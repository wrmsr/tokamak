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
package com.wrmsr.tokamak.core.tree.node;

import com.wrmsr.tokamak.core.tree.node.visitor.TNodeVisitor;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TJoinRelation
        extends TRelation
{
    private final TRelation left;
    private final TRelation right;
    private final Optional<TExpression> condition;

    public TJoinRelation(TRelation left, TRelation right, Optional<TExpression> condition)
    {
        this.left = checkNotNull(left);
        this.right = checkNotNull(right);
        this.condition = checkNotNull(condition);
    }

    public TRelation getLeft()
    {
        return left;
    }

    public TRelation getRight()
    {
        return right;
    }

    public Optional<TExpression> getCondition()
    {
        return condition;
    }

    @Override
    public <R, C> R accept(TNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitJoinRelation(this, context);
    }
}
