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
package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newLinkedHashMap;

@Immutable
public final class UnnestNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final String listField;
    private final Map<String, Type> unnestedFields;
    private final Optional<String> indexField;

    private final Map<String, Type> fields;

    @JsonCreator
    public UnnestNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source,
            @JsonProperty("listField") String listField,
            @JsonProperty("unnestedFields") Map<String, Type> unnestedFields,
            @JsonProperty("indexField") Optional<String> indexField)
    {
        super(name);

        this.source = checkNotNull(source);
        this.listField = checkNotNull(listField);
        this.unnestedFields = ImmutableMap.copyOf(unnestedFields);
        this.indexField = checkNotNull(indexField);

        Map<String, Type> fields = source.getFields();
        checkArgument(fields.containsKey(listField));
        if (indexField.isPresent()) {
            if (fields.containsKey(indexField.get())) {
                checkArgument(fields.get(indexField.get()) == Type.LONG);
            }
            else {
                fields = newLinkedHashMap(fields);
                fields.put(indexField.get(), Type.LONG);
            }
        }
        for (Map.Entry<String, Type> entry : unnestedFields.entrySet()) {
            checkArgument(!fields.containsKey(entry.getKey()));
            fields.put(entry.getKey(), entry.getValue());
        }

        this.fields = fields;

        checkInvariants();
    }

    @JsonProperty("source")
    @Override
    public Node getSource()
    {
        return source;
    }

    @JsonProperty("listField")
    public String getListField()
    {
        return listField;
    }

    @JsonProperty("unnestedFields")
    public Map<String, Type> getUnnestedFields()
    {
        return unnestedFields;
    }

    @JsonProperty("indexField")
    public Optional<String> getIndexField()
    {
        return indexField;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitUnnestNode(this, context);
    }
}
