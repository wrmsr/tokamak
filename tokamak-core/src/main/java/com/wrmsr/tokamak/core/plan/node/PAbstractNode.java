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
import com.wrmsr.tokamak.core.layout.RowLayout;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotations;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotations;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkUnique;

@Immutable
public abstract class PAbstractNode
        implements PNode
{
    private final String name;
    private final PNodeId nodeId;
    private final AnnotationCollection<PNodeAnnotation> annotations;
    private final AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations;

    protected PAbstractNode(
            String name,
            AnnotationCollection<PNodeAnnotation> annotations,
            AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations)
    {
        this.name = checkNotEmpty(name);
        this.nodeId = PNodeId.of(name);
        this.annotations = checkNotNull(annotations);
        this.fieldAnnotations = checkNotNull(fieldAnnotations);
    }

    protected void checkInvariants()
    {
        checkNotNull(getFields());
        checkUnique(getSources());
        checkState(getSources().isEmpty() == (this instanceof PLeaf));
        annotations.forEach(ann ->
                Optional.ofNullable(PNodeAnnotations.getValidatorsByAnnotationType().get(ann.getClass()))
                        .ifPresent(validator -> validator.accept(this)));
        fieldAnnotations.forEach((fld, anns) -> {
            checkState(getFields().getNames().contains(fld));
            Field field = checkNotNull(getFields().get(fld));
            anns.forEach(ann -> Optional.ofNullable(FieldAnnotations.getValidatorsByAnnotationType().get(ann.getClass()))
                    .ifPresent(validator -> validator.accept(field)));
        });

        if (this instanceof PInvalidator) {
            PInvalidator.checkInvariants((PInvalidator) this);
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", nodeId=" + nodeId +
                '}';
    }

    @JsonProperty("name")
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public PNodeId getId()
    {
        return nodeId;
    }

    @JsonProperty("annotations")
    @Override
    public AnnotationCollection<PNodeAnnotation> getAnnotations()
    {
        return annotations;
    }

    @JsonProperty("fieldAnnotations")
    @Override
    public AnnotationCollectionMap<String, FieldAnnotation> getFieldAnnotations()
    {
        return fieldAnnotations;
    }

    private final SupplierLazyValue<RowLayout> rowLayout = new SupplierLazyValue<>();

    @Override
    public RowLayout getRowLayout()
    {
        return rowLayout.get(() -> new RowLayout(getFields()));
    }
}
