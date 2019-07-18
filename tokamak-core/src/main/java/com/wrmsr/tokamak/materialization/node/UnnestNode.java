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
package com.wrmsr.tokamak.materialization.node;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.materialization.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newLinkedHashMap;

@Immutable
public final class UnnestNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final FieldName listField;
    private final Map<FieldName, Type> unnestedFields;
    private final Optional<FieldName> indexField;

    private final Map<FieldName, Type> fields;

    public UnnestNode(
            NodeName name,
            Node source,
            FieldName listField,
            Map<FieldName, Type> unnestedFields,
            Optional<FieldName> indexField)
    {
        super(name);

        Map<FieldName, Type> fields = source.getFields();
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
        for (Map.Entry<FieldName, Type> entry : unnestedFields.entrySet()) {
            checkArgument(!fields.containsKey(entry.getKey()));
            fields.put(entry.getKey(), entry.getValue());
        }

        this.source = source;
        this.listField = listField;
        this.unnestedFields = ImmutableMap.copyOf(unnestedFields);
        this.indexField = indexField;

        this.fields = fields;

        checkInvariants();
    }

    @Override
    public Node getSource()
    {
        return source;
    }

    public FieldName getListField()
    {
        return listField;
    }

    public Map<FieldName, Type> getUnnestedFields()
    {
        return unnestedFields;
    }

    public Optional<FieldName> getIndexField()
    {
        return indexField;
    }

    @Override
    public Map<FieldName, Type> getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitUnnestNode(this, context);
    }
}
