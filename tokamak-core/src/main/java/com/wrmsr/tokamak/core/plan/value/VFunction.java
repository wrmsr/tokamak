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
import com.wrmsr.tokamak.core.plan.node.PFunction;
import com.wrmsr.tokamak.core.plan.value.visitor.VNodeVisitor;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class VFunction
        implements VNode
{
    private final PFunction function;
    private final List<VNode> args;

    @JsonCreator
    public VFunction(
            @JsonProperty("function") PFunction function,
            @JsonProperty("args") List<VNode> args)
    {
        this.function = checkNotNull(function);
        this.args = ImmutableList.copyOf(args);
        checkState(this.args.size() == function.getType().getParams().size());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        VFunction vFunction = (VFunction) o;
        return Objects.equals(function, vFunction.function) &&
                Objects.equals(args, vFunction.args);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(function, args);
    }

    @Override
    public String toString()
    {
        return "Function{" +
                "function=" + function +
                ", args=" + args +
                '}';
    }

    @JsonProperty("function")
    public PFunction getFunction()
    {
        return function;
    }

    @JsonProperty("args")
    public List<VNode> getArgs()
    {
        return args;
    }

    @Override
    public <R, C> R accept(VNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFunction(this, context);
    }
}


