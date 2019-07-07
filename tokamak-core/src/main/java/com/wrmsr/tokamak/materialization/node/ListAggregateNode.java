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
public final class ListAggregateNode
        extends AbstractNode
        implements AggregateNode, SingleSourceNode
{
    private final Node source;
    private final FieldName groupField;
    private final Set<FieldName> listFields;

    public ListAggregateNode(
            NodeName name,
            Node source,
            FieldName groupField,
            Iterable<FieldName> listFields)
    {
        super(name);
        this.source = source;
        this.groupField = groupField;
        this.listFields = ImmutableSet.copyOf(listFields);

        checkInvariants();
    }

    @Override
    public Node getSource()
    {
        return source;
    }

    public FieldName getGroupField()
    {
        return groupField;
    }

    public Set<FieldName> getListFields()
    {
        return listFields;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitListAggregateNode(this, context);
    }
}
