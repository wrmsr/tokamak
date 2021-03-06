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
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.collect.OrderPreservingImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;

@Immutable
public final class PScan
        extends PAbstractNode
        implements PLeaf
{
    private final SchemaTable schemaTable;
    private final FieldCollection fields;
    private final PInvalidations invalidations;

    @JsonCreator
    private PScan(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") AnnotationCollection<PNodeAnnotation> annotations,
            @JsonProperty("fieldAnnotations") AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations,
            @JsonProperty("schemaTable") SchemaTable schemaTable,
            @JsonProperty("fields") OrderPreservingImmutableMap<String, Type> fields,
            @JsonProperty("invalidations") PInvalidations invalidations)
    {
        super(name, annotations, fieldAnnotations);

        checkNotNull(fields);

        this.schemaTable = checkNotNull(schemaTable);
        this.fields = FieldCollection.of(checkNotNull(fields), fieldAnnotations);
        this.invalidations = invalidations;

        checkInvariants();
    }

    // FIXME: P.java
    public PScan(
            String name,
            AnnotationCollection<PNodeAnnotation> annotations,
            AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations,
            SchemaTable schemaTable,
            Map<String, Type> fields,
            PInvalidations invalidations)
    {
        this(
                name,
                annotations,
                fieldAnnotations,
                schemaTable,
                new OrderPreservingImmutableMap<>(checkOrdered(fields)),
                invalidations);
    }

    @JsonProperty("schemaTable")
    public SchemaTable getSchemaTable()
    {
        return schemaTable;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @JsonProperty("fields")
    public OrderPreservingImmutableMap<String, Type> getScanFields()
    {
        return new OrderPreservingImmutableMap<>(fields.getTypesByName());
    }

    @JsonProperty("invalidations")
    @Override
    public PInvalidations getInvalidations()
    {
        return invalidations;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitScan(this, context);
    }
}
