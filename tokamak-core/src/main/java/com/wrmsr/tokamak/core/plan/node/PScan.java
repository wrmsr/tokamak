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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.FieldAnnotations;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.util.collect.OrderPreservingImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.core.layout.field.FieldCollection.toFieldCollection;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;

@Immutable
public final class PScan
        extends PAbstractNode
        implements PGenerator
{
    private final SchemaTable schemaTable;
    private final FieldCollection fields;
    private final Set<String> idFields;
    private final Set<String> idNodes;

    @JsonCreator
    private PScan(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("schemaTable") SchemaTable schemaTable,
            @JsonProperty("fields") OrderPreservingImmutableMap<String, Type> fields,
            @JsonProperty("idFields") Set<String> idFields,
            @JsonProperty("idNodes") Set<String> idNodes)
    {
        super(name, annotations);

        this.schemaTable = checkNotNull(schemaTable);
        this.fields = checkNotNull(fields).entrySet().stream()
                .map(e -> new Field(e.getKey(), e.getValue(), new FieldAnnotations(ImmutableList.of(FieldAnnotation.id()))))
                .collect(toFieldCollection());
        this.idFields = ImmutableSet.copyOf(idFields);
        this.idNodes = ImmutableSet.copyOf(idNodes);

        this.idFields.forEach(f -> checkState(this.fields.contains(f)));

        checkInvariants();
    }

    // FIXME: P.java
    public PScan(
            String name,
            PNodeAnnotations annotations,
            SchemaTable schemaTable,
            Map<String, Type> fields,
            Set<String> idFields,
            Set<String> idNodes)
    {
        this(
                name,
                annotations,
                schemaTable,
                new OrderPreservingImmutableMap<>(checkOrdered(fields)),
                idFields,
                idNodes);
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
    private OrderPreservingImmutableMap<String, Type> getJsonFields()
    {
        return new OrderPreservingImmutableMap<>(fields.getTypesByName());
    }

    @JsonProperty("idFields")
    public Set<String> getIdFields()
    {
        return idFields;
    }

    @JsonProperty("idNodes")
    public Set<String> getIdNodes()
    {
        return idNodes;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitScan(this, context);
    }
}
