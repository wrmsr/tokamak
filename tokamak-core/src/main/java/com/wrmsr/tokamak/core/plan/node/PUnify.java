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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PUnify
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final Set<String> unifiedFields;
    private final String outputField;

    private final FieldCollection fields;

    @JsonCreator
    public PUnify(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("unifiedFields") Set<String> unifiedFields,
            @JsonProperty("outputField") String outputField)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.unifiedFields = checkNotEmpty(ImmutableSet.copyOf(unifiedFields));
        this.outputField = checkNotEmpty(outputField);

        Type unifiedType = source.getFields().getType(checkNotNull(this.unifiedFields.iterator().next()));
        this.unifiedFields.forEach(uf -> {
            checkNotEmpty(uf);
            checkState(source.getFields().getType(uf).equals(unifiedType));
        });

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("unifiedFields")
    public Set<String> getUnifiedFields()
    {
        return unifiedFields;
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
        return visitor.visitUnify(this, context);
    }
}
