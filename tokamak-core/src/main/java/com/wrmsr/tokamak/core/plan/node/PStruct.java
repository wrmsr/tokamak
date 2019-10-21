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
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.impl.StructType;

import javax.annotation.concurrent.Immutable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PStruct
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final StructType type;
    private final List<String> inputFields;
    private final String outputField;

    private final FieldCollection fields;

    @JsonCreator
    public PStruct(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("type") StructType type,
            @JsonProperty("inputFields") List<String> inputFields,
            @JsonProperty("outputField") String outputField)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.type = checkNotNull(type);
        this.inputFields = ImmutableList.copyOf(inputFields);
        this.outputField = checkNotEmpty(outputField);

        checkState(this.inputFields.size() == type.getMembers().size());
        for (int i = 0; i < type.getMembers().size(); ++i) {
            Type st = type.getMembers().get(i).getType();
            Type ft = source.getFields().getType(inputFields.get(i));
            checkState(st.equals(ft));
        }
        checkState(!source.getFields().contains(outputField));

        fields = FieldCollection.builder()
                .addAll(source.getFields())
                .add(outputField, type)
                .build();

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("type")
    public StructType getType()
    {
        return type;
    }

    @JsonProperty("inputFields")
    public List<String> getInputFields()
    {
        return inputFields;
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
        return visitor.visitStruct(this, context);
    }
}
