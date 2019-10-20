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
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.impl.StructType;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PExtract
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final String sourceField;
    private final String structMember;
    private final String outputField;

    private final FieldCollection fields;

    @JsonCreator
    public PExtract(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("sourceField") String sourceField,
            @JsonProperty("structMember") String structMember,
            @JsonProperty("outputField") String outputField)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.sourceField = checkNotEmpty(sourceField);
        this.structMember = checkNotEmpty(structMember);
        this.outputField = checkNotEmpty(outputField);

        Field sourceFieldObj = checkNotNull(source.getFields().get(sourceField));
        StructType structType = (StructType) checkNotNull(sourceFieldObj.getType());
        Type structMemberType = structType.getMember(structMember);
        checkState(!source.getFields().contains(outputField));

        fields = FieldCollection.builder()
                .addAll(source.getFields())
                .add(outputField, structMemberType)
                .build();

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("sourceField")
    public String getSourceField()
    {
        return sourceField;
    }

    @JsonProperty("structMember")
    public String getStructMember()
    {
        return structMember;
    }

    @JsonProperty("outputField")
    public String getOutputField()
    {
        return outputField;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitExtract(this, context);
    }
}
