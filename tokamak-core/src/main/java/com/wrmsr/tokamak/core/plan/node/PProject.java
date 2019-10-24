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
import static com.google.common.base.Preconditions.checkState;

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
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("projection") PProjection projection)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.projection = checkNotNull(projection);

        projection.getInputsByOutput().values().forEach(this::checkValue);

        ImmutableMap.Builder<String, Type> fields = ImmutableMap.builder();
        for (Map.Entry<String, PValue> entry : projection.getInputsByOutput().entrySet()) {
            fields.put(entry.getKey(), getValueType(entry.getValue()));
        }
        this.fields = FieldCollection.of(fields.build()).withAnnotations(annotations.getFields());

        checkInvariants();
    }

    private Type getValueType(PValue value)
    {
        if (value instanceof PValue.Constant) {
            return ((PValue.Constant) value).getType();
        }
        else if (value instanceof PValue.Field) {
            return source.getFields().getType(((PValue.Field) value).getField());
        }
        else if (value instanceof PValue.Function) {
            return ((PValue.Function) value).getFunction().getType().getReturnType();
        }
        else {
            throw new IllegalArgumentException(Objects.toString(value));
        }
    }

    private void checkValue(PValue value)
    {
        if (value instanceof PValue.Constant) {
            // pass
        }
        else if (value instanceof PValue.Field) {
            checkState(source.getFields().contains(((PValue.Field) value).getField()));
        }
        else if (value instanceof PValue.Function) {
            ((PValue.Function) value).getArgs().forEach(this::checkValue);
        }
        else {
            throw new IllegalArgumentException(Objects.toString(value));
        }
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
