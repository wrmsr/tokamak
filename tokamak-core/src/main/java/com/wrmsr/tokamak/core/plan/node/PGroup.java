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
import com.wrmsr.tokamak.core.layout.field.FieldAnnotations;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.impl.ListType;
import com.wrmsr.tokamak.core.type.impl.StructType;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PGroup
        extends PAbstractNode
        implements PAggregate, PSingleSource
{
    private final PNode source;
    private final List<String> keyFields;
    private final String listField;

    private final StructType structType;
    private final FieldCollection fields;

    @JsonCreator
    public PGroup(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("keyFields") List<String> keyFields,
            @JsonProperty("listField") String listField)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.keyFields = ImmutableList.copyOf(keyFields);
        this.listField = checkNotNull(listField);

        checkArgument(!keyFields.contains(listField));
        checkArgument(source.getFields().containsAll(keyFields));

        structType = new StructType(ImmutableMap.copyOf(source.getFields().getTypesByName()));
        fields = FieldCollection.builder()
                .addAll(keyFields.stream()
                        .map(f -> new Field(f, source.getFields().getType(f), new FieldAnnotations(ImmutableList.of(FieldAnnotation.id())))))
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

    @JsonProperty("keyFields")
    public List<String> getKeyFields()
    {
        return keyFields;
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
        return visitor.visitGroup(this, context);
    }
}
