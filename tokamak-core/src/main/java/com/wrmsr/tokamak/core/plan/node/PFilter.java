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
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.Types;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PFilter
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final PFunction function;
    private final List<String> args;
    private final boolean unlinked;

    @JsonCreator
    public PFilter(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("function") PFunction function,
            @JsonProperty("args") List<String> args,
            @JsonProperty("unlinked") boolean unlinked)
    {
        super(name, annotations);

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
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("function")
    public PFunction getFunction()
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
    public FieldCollection getFields()
    {
        return source.getFields();
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFilter(this, context);
    }
}
