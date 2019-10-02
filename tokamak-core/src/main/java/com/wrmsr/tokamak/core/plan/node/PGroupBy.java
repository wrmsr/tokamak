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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.impl.ListType;
import com.wrmsr.tokamak.core.type.impl.StructType;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PGroupBy
        extends PAbstractNode
        implements PAggregate, PSingleSource
{
    private final PNode source;
    private final String groupField;
    private final String listField;

    private final StructType structType;
    private final FieldCollection fields;

    @JsonCreator
    public PGroupBy(
            @JsonProperty("name") String name,
            @JsonProperty("source") PNode source,
            @JsonProperty("groupField") String groupField,
            @JsonProperty("listField") String listField)
    {
        super(name);

        this.source = checkNotNull(source);
        this.groupField = checkNotNull(groupField);
        this.listField = checkNotNull(listField);

        checkArgument(!groupField.equals(listField));
        checkArgument(source.getFields().contains(groupField));

        structType = new StructType(ImmutableMap.copyOf(source.getFields().getTypesByName()));
        fields = FieldCollection.builder()
                .add(new Field(groupField, source.getFields().getType(groupField), ImmutableList.of(FieldAnnotation.id())))
                .add(listField, new ListType(structType))
                .build();

        // FIXME:
        // checkState(fields.get(groupField).hasAnnotation(IdField.class));

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
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
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitGroupBy(this, context);
    }
}
