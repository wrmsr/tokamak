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
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.search2.search.node.visitor.SNodeVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.UnaryOperator.identity;

public final class SCompare
        extends SAbstractNode
        implements SOperator
{
    public enum Op
    {
        EQ("=="),
        NE("!="),
        GT(">"),
        GE(">="),
        LT("<"),
        LE("<=");

        private final String string;

        Op(String string)
        {
            this.string = checkNotEmpty(string);
        }

        public String getString()
        {
            return string;
        }

        public static final Map<String, Op> STRING_MAP = Arrays.stream(Op.values()).collect(toImmutableMap(Op::getString, identity()));

        public static Op fromString(String str)
        {
            return checkNotNull(STRING_MAP.get(checkNotEmpty(str)));
        }
    }

    private final Op op;
    private final SNode left;
    private final SNode right;

    @JsonCreator
    public SCompare(
            @JsonProperty("op") Op op,
            @JsonProperty("left") SNode left,
            @JsonProperty("right") SNode right)
    {
        this.op = checkNotNull(op);
        this.left = checkNotNull(left);
        this.right = checkNotNull(right);
    }

    @JsonProperty("op")
    public Op getOp()
    {
        return op;
    }

    @JsonProperty("left")
    public SNode getLeft()
    {
        return left;
    }

    @JsonProperty("right")
    public SNode getRight()
    {
        return right;
    }

    @Override
    public List<SNode> getSources()
    {
        return ImmutableList.of(left, right);
    }

    @Override
    public <R, C> R accept(SNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitCompare(this, context);
    }
}
