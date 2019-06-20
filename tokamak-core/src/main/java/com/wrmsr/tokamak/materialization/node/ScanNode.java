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
import com.wrmsr.tokamak.materialization.api.TableName;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Set;

@Immutable
public final class ScanNode
        extends StatefulNode
        implements GeneratorNode
{
    private final TableName table;
    private final Set<FieldName> fields;

    public ScanNode(
            NodeName name,
            TableName table,
            Iterable<FieldName> fields,
            Map<NodeName, Invalidation> invalidations,
            Map<NodeName, LinkageMask> linkageMasks)
    {
        super(name, invalidations, linkageMasks);

        this.table = table;
        this.fields = ImmutableSet.copyOf(fields);

        checkInvariants();
    }

    public TableName getTable()
    {
        return table;
    }

    public Set<FieldName> getFields()
    {
        return fields;
    }

    @Override
    public <C, R> R accept(NodeVisitor<C, R> visitor, C context)
    {
        return visitor.visitScanNode(this, context);
    }
}
