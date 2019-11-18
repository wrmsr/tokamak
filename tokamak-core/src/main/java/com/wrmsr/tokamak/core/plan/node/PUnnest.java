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
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.util.collect.OrderPreservingImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;

@Immutable
public final class PUnnest
        extends PAbstractNode
        implements PIndexable, PSingleSource
{
    private final PNode source;
    private final String listField;
    private final Map<String, Type> unnestedFields;
    private final Optional<String> indexField;

    private final FieldCollection fields;

    @JsonCreator
    public PUnnest(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("listField") String listField,
            @JsonProperty("unnestedFields") Map<String, Type> unnestedFields,
            @JsonProperty("indexField") Optional<String> indexField)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.listField = checkNotNull(listField);
        this.unnestedFields = ImmutableMap.copyOf(checkOrdered(unnestedFields));
        this.indexField = checkNotNull(indexField);

        Map<String, Type> fields = source.getFields().getTypesByName();
        checkArgument(fields.containsKey(listField));
        if (indexField.isPresent()) {
            if (fields.containsKey(indexField.get())) {
                checkArgument(fields.get(indexField.get()) == Types.LONG);
            }
            else {
                fields = newLinkedHashMap(fields);
                fields.put(indexField.get(), Types.LONG);
            }
        }
        for (Map.Entry<String, Type> entry : this.unnestedFields.entrySet()) {
            checkArgument(!fields.containsKey(entry.getKey()));
            fields.put(entry.getKey(), entry.getValue());
        }

        this.fields = FieldCollection.of(fields);

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("listField")
    public String getListField()
    {
        return listField;
    }

    @JsonSerialize(using = OrderPreservingImmutableMap.Serializer.class)
    @JsonDeserialize(using = OrderPreservingImmutableMap.Deserializer.class)
    @JsonProperty("unnestedFields")
    public Map<String, Type> getUnnestedFields()
    {
        return unnestedFields;
    }

    @JsonProperty("indexField")
    @Override
    public Optional<String> getIndexField()
    {
        return indexField;
    }

    @Override
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitUnnest(this, context);
    }
}
