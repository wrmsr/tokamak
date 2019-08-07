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

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class ProjectNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final Projection projection;

    private final Map<String, Type> fields;

    public ProjectNode(
            String name,
            Node source,
            Projection projection)
    {
        super(name);

        ImmutableMap.Builder<String, Type> fields = ImmutableMap.builder();

        for (Map.Entry<String, Projection.Input> entry : projection.getInputsByOutput().entrySet()) {
            if (entry.getValue() instanceof Projection.FieldInput) {
                String inputField = ((Projection.FieldInput) entry.getValue()).getField();
                checkArgument(source.getFields().containsKey(inputField));
                fields.put(entry.getKey(), source.getFields().get(inputField));
            }
            else if (entry.getValue() instanceof Projection.FunctionInput) {
                fields.put(entry.getKey(), ((Projection.FunctionInput) entry.getValue()).getType());
            }
            else {
                throw new IllegalArgumentException(entry.getValue().toString());
            }
        }

        this.source = source;
        this.projection = projection;

        this.fields = fields.build();

        checkInvariants();
    }

    @Override
    public Node getSource()
    {
        return source;
    }

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
    public Set<Set<String>> getIdFieldSets()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitProjectNode(this, context);
    }
}
