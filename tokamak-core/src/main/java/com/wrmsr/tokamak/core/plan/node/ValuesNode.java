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
import com.wrmsr.tokamak.core.plan.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.core.type.Type;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.util.collect.OrderPreservingImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Immutable
public final class ValuesNode
        extends AbstractNode
        implements GeneratorNode
{
    private final Map<String, Type> declaredFields;
    private final List<List<Object>> values;
    private final Optional<String> indexField;
    private final boolean weak;

    private final Map<String, Type> fields;

    @JsonCreator
    public ValuesNode(
            @JsonProperty("name") String name,
            @JsonProperty("fields") Map<String, Type> fields,
            @JsonProperty("values") List<List<Object>> values,
            @JsonProperty("indexField") Optional<String> indexField,
            @JsonProperty("weak") boolean weak)
    {
        super(name);

        this.declaredFields = ImmutableMap.copyOf(fields);
        this.values = checkNotNull(values).stream().map(ImmutableList::copyOf).collect(toImmutableList());
        this.indexField = indexField;
        this.weak = weak;

        ImmutableMap.Builder<String, Type> fieldsBuilder = ImmutableMap.builder();
        fieldsBuilder.putAll(this.declaredFields);
        indexField.ifPresent(f -> fieldsBuilder.put(f, Types.LONG));
        this.fields = fieldsBuilder.build();
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
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @JsonProperty("values")
    public List<List<Object>> getValues()
    {
        return values;
    }

    @JsonProperty("indexField")
    public Optional<String> getIndexField()
    {
        return indexField;
    }

    @JsonProperty("weak")
    public boolean isWeak()
    {
        return weak;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitValuesNode(this, context);
    }
}
