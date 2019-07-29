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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newLinkedHashMap;

@Immutable
public final class ValuesNode
        extends AbstractNode
        implements GeneratorNode
{
    private final Map<String, Type> fields;
    private final List<Object> values;
    private final Optional<String> indexField;
    private final boolean isStrict;

    public ValuesNode(
            String name,
            Map<String, Type> fields,
            List<Object> values,
            Optional<String> indexField,
            Optional<Boolean> isStrict)
    {
        super(name);

        if (indexField.isPresent()) {
            if (fields.containsKey(indexField.get())) {
                checkArgument(fields.get(indexField.get()) == Type.LONG);
            }
            else {
                fields = newLinkedHashMap(fields);
                fields.put(indexField.get(), Type.LONG);
            }
        }

        this.fields = ImmutableMap.copyOf(fields);
        this.values = ImmutableList.copyOf(values);
        this.indexField = indexField;
        this.isStrict = isStrict.orElse(false);

        checkInvariants();
    }

    public List<Object> getValues()
    {
        return values;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return fields;
    }

    public Optional<String> getIndexField()
    {
        return indexField;
    }

    public boolean isStrict()
    {
        return isStrict;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitValuesNode(this, context);
    }
}
