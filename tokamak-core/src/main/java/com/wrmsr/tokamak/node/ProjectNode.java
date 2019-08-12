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
package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ProjectNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final Projection projection;

    private final Map<String, Type> fields;

    @JsonCreator
    public ProjectNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source,
            @JsonProperty("projection") Projection projection)
    {
        super(name);

        this.source = checkNotNull(source);
        this.projection = checkNotNull(projection);

        ImmutableMap.Builder<String, Type> fields = ImmutableMap.builder();
        for (Map.Entry<String, Projection.Input> entry : projection.getInputsByOutput().entrySet()) {
            if (entry.getValue() instanceof Projection.FieldInput) {
                String inputField = ((Projection.FieldInput) entry.getValue()).getField();
                checkArgument(source.getFields().containsKey(inputField));
                fields.put(entry.getKey(), source.getFields().get(inputField));
            }
            else if (entry.getValue() instanceof Projection.FunctionInput) {
                Projection.FunctionInput functionInput = (Projection.FunctionInput) entry.getValue();
                functionInput.getArgs().forEach(f -> checkArgument(source.getFields().containsKey(f)));
                fields.put(entry.getKey(), functionInput.getType());
            }
            else {
                throw new IllegalArgumentException(entry.getValue().toString());
            }
        }
        this.fields = fields.build();

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public Node getSource()
    {
        return source;
    }

    @JsonProperty("projection")
    public Projection getProjection()
    {
        return projection;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitProjectNode(this, context);
    }
}
