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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.PAbstractNode;
import com.wrmsr.tokamak.core.plan.node.PIndexable;
import com.wrmsr.tokamak.core.plan.node.PInvalidations;
import com.wrmsr.tokamak.core.plan.node.PLeaf;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.collect.OrderPreservingImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Immutable
public final class PValues
        extends PAbstractNode
        implements PLeaf, PIndexable
{
    public enum Strictness
    {
        STRICT,
        NON_STRICT,
    }

    private final Map<String, Type> declaredFields;
    private final List<List<Object>> values;
    private final Optional<String> indexField;
    private final Strictness strictness;

    private final FieldCollection fields;

    @JsonCreator
    public PValues(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") AnnotationCollection<PNodeAnnotation> annotations,
            @JsonProperty("fieldAnnotations") AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations,
            @JsonProperty("fields") Map<String, Type> fields,
            @JsonProperty("values") List<List<Object>> values,
            @JsonProperty("indexField") Optional<String> indexField,
            @JsonProperty("strictness") Strictness strictness)
    {
        super(name, annotations, fieldAnnotations);

        this.declaredFields = ImmutableMap.copyOf(fields);
        this.values = checkNotNull(values).stream().map(ImmutableList::copyOf).collect(toImmutableList());
        this.indexField = checkNotNull(indexField);
        this.strictness = checkNotNull(strictness);

        ImmutableMap.Builder<String, Type> fieldsBuilder = ImmutableMap.builder();
        fieldsBuilder.putAll(this.declaredFields);
        indexField.ifPresent(f -> fieldsBuilder.put(f, Types.Long()));
        this.fields = FieldCollection.of(fieldsBuilder.build());
        this.values.forEach(l -> checkArgument(l.size() == fields.size()));

        checkInvariants();
    }

    @JsonSerialize(using = OrderPreservingImmutableMap.Serializer.class)
    @JsonDeserialize(using = OrderPreservingImmutableMap.Deserializer.class)
    @JsonProperty("fields")
    public Map<String, Type> getDeclaredFields()
    {
        return declaredFields;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @JsonProperty("values")
    public List<List<Object>> getValues()
    {
        return values;
    }

    @JsonProperty("indexField")
    @Override
    public Optional<String> getIndexField()
    {
        return indexField;
    }

    @JsonProperty("strictness")
    public Strictness getStrictness()
    {
        return strictness;
    }

    @Override
    public PInvalidations getInvalidations()
    {
        return PInvalidations.empty();
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitValues(this, context);
    }
}
