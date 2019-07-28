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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.FieldName;
import com.wrmsr.tokamak.api.NodeName;
import com.wrmsr.tokamak.api.TableName;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

@Immutable
public final class ScanNode
        extends StatefulNode
        implements GeneratorNode
{
    private final TableName table;
    private final Map<FieldName, Type> fields;

    public ScanNode(
            @JsonProperty("name") NodeName name,
            @JsonProperty("table") TableName table,
            @JsonProperty("fields") Map<FieldName, Type> fields,
            Map<NodeName, Invalidation> invalidations,
            Map<NodeName, LinkageMask> linkageMasks,
            List<NodeName> idNodes)
    {
        super(name, invalidations, linkageMasks);

        this.table = table;
        this.fields = ImmutableMap.copyOf(fields);

        checkInvariants();
    }

    @JsonProperty("table")
    public TableName getTable()
    {
        return table;
    }

    @JsonProperty("fields")
    public Map<FieldName, Type> getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitScanNode(this, context);
    }
}
