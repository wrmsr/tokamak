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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PCache
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;

    private final FieldCollection fields;

    public PCache(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") AnnotationCollection<PNodeAnnotation> annotations,
            @JsonProperty("fieldAnnotations") AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations,
            @JsonProperty("source") PNode source)
    {
        super(name, annotations, fieldAnnotations);

        this.source = checkNotNull(source);

        FieldCollection.of(source.getFields(), source.getFields().getTransitiveAnnotations().update(fieldAnnotations));
        this.fields = source.getFields()
                .withOnlyTransitiveAnnotations()
                .withAnnotations(annotations.getFieldAnnotations());

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitCache(this, context);
    }
}
