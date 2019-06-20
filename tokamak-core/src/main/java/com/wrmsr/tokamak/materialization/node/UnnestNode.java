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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

@Immutable
public final class UnnestNode
        extends AbstractNode
        implements SingleSourceNode
{
    private final Node source;
    private final FieldName listField;
    private final Set<FieldName> unnestedFields;

    public UnnestNode(
            NodeName name,
            Node source,
            FieldName listField,
            Iterable<FieldName> unnestedFields)
    {
        super(name);
        this.source = source;
        this.listField = listField;
        this.unnestedFields = ImmutableSet.copyOf(unnestedFields);

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

    public Set<FieldName> getUnnestedFields()
    {
        return unnestedFields;
    }

    @Override
    public <C, R> R accept(NodeVisitor<C, R> visitor, C context)
    {
        return visitor.visitUnnestNode(this, context);
    }
}
