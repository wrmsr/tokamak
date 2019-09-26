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
package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.plan.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.Types;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class FilterNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final Function function;
    private final List<String> args;
    private final boolean unlinked;

    @JsonCreator
    public FilterNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source,
            @JsonProperty("function") Function function,
            @JsonProperty("args") List<String> args,
            @JsonProperty("unlinked") boolean unlinked)
    {
        super(name);

        this.source = checkNotNull(source);
        this.function = checkNotNull(function);
        this.args = ImmutableList.copyOf(args);
        this.unlinked = unlinked;

        checkArgument(function.getType().getReturnType() == Types.BOOLEAN);
        checkArgument(function.getType().getParamTypes().size() == this.args.size());

        // FIXME: check
        // function.getSignature().getParams().forEach((f, t) -> {
        //     Type st = source.getFields().get(f);
        //     checkNotNull(st);
        //     checkArgument(st.equals(t));
        // });

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public Node getSource()
    {
        return source;
    }

    @JsonProperty("function")
    public Function getFunction()
    {
        return function;
    }

    @JsonProperty("args")
    public List<String> getArgs()
    {
        return args;
    }

    @JsonProperty("unlinked")
    public boolean isUnlinked()
    {
        return unlinked;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return source.getFields();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFilterNode(this, context);
    }
}
