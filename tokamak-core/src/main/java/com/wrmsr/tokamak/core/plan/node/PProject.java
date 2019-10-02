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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PProject
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final PProjection projection;

    private final FieldCollection fields;

    @JsonCreator
    public PProject(
            @JsonProperty("name") String name,
            @JsonProperty("source") PNode source,
            @JsonProperty("projection") PProjection projection)
    {
        super(name);

        this.source = checkNotNull(source);
        this.projection = checkNotNull(projection);

        ImmutableMap.Builder<String, Type> fields = ImmutableMap.builder();
        for (Map.Entry<String, PProjection.Input> entry : projection.getInputsByOutput().entrySet()) {
            if (entry.getValue() instanceof PProjection.FieldInput) {
                String inputField = ((PProjection.FieldInput) entry.getValue()).getField();
                checkArgument(source.getFields().contains(inputField));
                fields.put(entry.getKey(), source.getFields().getType(inputField));
            }
            else if (entry.getValue() instanceof PProjection.FunctionInput) {
                // FIXME: check types
                PProjection.FunctionInput functionInput = (PProjection.FunctionInput) entry.getValue();
                functionInput.getArgs().forEach(f -> checkArgument(source.getFields().contains(f)));
                fields.put(entry.getKey(), functionInput.getFunction().getType().getReturnType());
            }
            else {
                throw new IllegalArgumentException(Objects.toString(entry.getValue()));
            }
        }
        this.fields = FieldCollection.of(fields.build());

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("projection")
    public PProjection getProjection()
    {
        return projection;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitProject(this, context);
    }
}
