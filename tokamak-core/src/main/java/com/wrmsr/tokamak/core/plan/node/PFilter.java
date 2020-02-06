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
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.TypeAnnotations;
import com.wrmsr.tokamak.core.type.hier.primitive.BooleanType;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PFilter
        extends PAbstractNode
        implements PSingleSource
{
    public enum Linking
    {
        LINKED,
        UNLINKED,
    }

    private final PNode source;
    private final String field;
    private final Linking linking;

    private final FieldCollection fields;

    @JsonCreator
    public PFilter(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") AnnotationCollection<PNodeAnnotation> annotations,
            @JsonProperty("fieldAnnotations") AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("field") String field,
            @JsonProperty("linking") Linking linking)
    {
        super(name, annotations, fieldAnnotations);

        this.source = checkNotNull(source);
        this.field = checkNotNull(field);
        this.linking = checkNotNull(linking);

        checkArgument(TypeAnnotations.strip(source.getFields().getType(field)) instanceof BooleanType);

        this.fields = FieldCollection.of(source.getFields().getTypesByName(), fieldAnnotations);

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("field")
    public String getField()
    {
        return field;
    }

    @JsonProperty("linking")
    public Linking getLinking()
    {
        return linking;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitFilter(this, context);
    }
}
