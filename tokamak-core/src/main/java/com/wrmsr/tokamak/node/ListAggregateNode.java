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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.RowType;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class ListAggregateNode
        extends AbstractNode
        implements AggregateNode, SingleSourceNode
{
    private final Node source;
    private final String groupField;
    private final String listField;

    private final RowType rowType;
    private final Map<String, Type> fields;

    @JsonCreator
    public ListAggregateNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source,
            @JsonProperty("groupField") String groupField,
            @JsonProperty("listField") String listField)
    {
        super(name);

        this.source = source;
        this.groupField = groupField;
        this.listField = listField;

        checkArgument(!groupField.equals(listField));
        checkArgument(source.getFields().containsKey(groupField));

        rowType = new RowType(source.getFields());
        fields = ImmutableMap.of(
                groupField, source.getFields().get(groupField),
                listField, rowType
        );

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public Node getSource()
    {
        return source;
    }

    @JsonProperty("groupField")
    public String getGroupField()
    {
        return groupField;
    }

    @JsonProperty("listField")
    public String getListField()
    {
        return listField;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        return ImmutableSet.of(ImmutableSet.of(groupField));
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitListAggregateNode(this, context);
    }
}
