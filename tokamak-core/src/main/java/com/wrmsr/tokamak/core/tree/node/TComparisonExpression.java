/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http,
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wrmsr.tokamak.core.tree.node;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.tree.node.visitor.TNodeVisitor;
import com.wrmsr.tokamak.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class TComparisonExpression
        extends TExpression
{
    public enum Op
    {
        EQ("="),
        NE("!=", "<>"),
        GT(">"),
        GE(">="),
        LT("<"),
        LE("<=");

        private final List<String> strings;

        Op(String... strings)
        {
            this.strings = checkNotEmpty(ImmutableList.copyOf(strings));
        }

        public String getString()
        {
            return strings.get(0);
        }

        public List<String> getStrings()
        {
            return strings;
        }

        public static final Map<String, Op> STRING_MAP = Arrays.stream(Op.values())
                .flatMap(o -> o.strings.stream().map(s -> Pair.immutable(s, o)))
                .collect(toImmutableMap());

        public static Op fromString(String str)
        {
            return checkNotNull(STRING_MAP.get(checkNotEmpty(str)));
        }
    }

    private final TExpression left;
    private final Op op;
    private final TExpression right;

    public TComparisonExpression(TExpression left, Op op, TExpression right)
    {
        this.left = checkNotNull(left);
        this.op = checkNotNull(op);
        this.right = checkNotNull(right);
    }

    public TExpression getLeft()
    {
        return left;
    }

    public Op getOp()
    {
        return op;
    }

    public TExpression getRight()
    {
        return right;
    }

    @Override
    public <R, C> R accept(TNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitComparisonExpression(this, context);
    }
}
