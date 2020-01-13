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
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Immutable
public final class PProject
        extends PAbstractNode
        implements PSingleSource
{
    private final PNode source;
    private final PProjection projection;

    private final FieldCollection fields;

    @JsonCreator
    public PProject(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") AnnotationCollection<PNodeAnnotation> annotations,
            @JsonProperty("fieldAnnotations") AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("projection") PProjection projection)
    {
        super(name, annotations, fieldAnnotations);

        this.source = checkNotNull(source);
        this.projection = checkNotNull(projection);

        projection.getInputsByOutput().values().forEach(this::checkValue);

        FieldCollection.Builder fields = FieldCollection.builder();
        projection.getInputsByOutput().forEach((field, input) -> {
            AnnotationCollection<FieldAnnotation> fldAnns = AnnotationCollection.of();

            if (input instanceof PValue.Field) {
                String srcField = ((PValue.Field) input).getField();
                AnnotationCollection<FieldAnnotation> srcFldAnns = source.getFields().getTransitiveAnnotations().getOrEmpty(srcField);
                fldAnns = fldAnns.merged(srcFldAnns);
            }

            fldAnns = fldAnns.merged(fieldAnnotations.getOrEmpty(field));

            fields.add(new Field(field, getValueType(input), fldAnns));
        });

        this.fields = fields.build();

        checkInvariants();
    }

    private Type getValueType(PValue value)
    {
        if (value instanceof PValue.Constant) {
            return ((PValue.Constant) value).getType();
        }
        else if (value instanceof PValue.Field) {
            return source.getFields().getType(((PValue.Field) value).getField());
        }
        else if (value instanceof PValue.Function) {
            return ((PValue.Function) value).getFunction().getType().getReturnType();
        }
        else {
            throw new IllegalArgumentException(Objects.toString(value));
        }
    }

    private void checkValue(PValue value)
    {
        if (value instanceof PValue.Constant) {
            // pass
        }
        else if (value instanceof PValue.Field) {
            checkState(source.getFields().contains(((PValue.Field) value).getField()));
        }
        else if (value instanceof PValue.Function) {
            ((PValue.Function) value).getArgs().forEach(this::checkValue);
        }
        else {
            throw new IllegalArgumentException(Objects.toString(value));
        }
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("projection")
    public PProjection getProjection()
    {
        return projection;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitProject(this, context);
    }

    private final SupplierLazyValue<Set<String>> addedFields = new SupplierLazyValue<>();

    public Set<String> getAddedFields()
    {
        return addedFields.get(() -> ImmutableSet.copyOf(Sets.difference(projection.getInputsByOutput().keySet(), source.getFields().getNames())));
    }

    private final SupplierLazyValue<Set<String>> droppedFields = new SupplierLazyValue<>();

    public Set<String> getDroppedFields()
    {
        return droppedFields.get(() -> ImmutableSet.copyOf(Sets.difference(source.getFields().getNames(), projection.getInputsByOutput().keySet())));
    }
}
