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

package com.wrmsr.tokamak.core.plan.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.plan.value.visitor.VNodeVisitor;
import com.wrmsr.tokamak.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class VSpecial
        implements VNode
{
    public enum Op
    {
        AND("and"),
        OR("or");

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

    private final Op op;
    private final List<VNode> args;

    @JsonCreator
    public VSpecial(
            @JsonProperty("op") Op op,
            @JsonProperty("args") List<VNode> args)
    {
        this.op = checkNotNull(op);
        this.args = ImmutableList.copyOf(args);
    }

    @JsonProperty("op")
    public Op getOp()
    {
        return op;
    }

    @JsonProperty("args")
    public List<VNode> getArgs()
    {
        return args;
    }

    @Override
    public <R, C> R accept(VNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitSpecial(this, context);
    }
}
