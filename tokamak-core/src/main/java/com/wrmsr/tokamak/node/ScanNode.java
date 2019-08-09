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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ScanNode
        extends StatefulNode
        implements GeneratorNode
{
    private final SchemaTable table;
    private final Map<String, Type> fields;
    private final Set<String> idNodes;

    @JsonCreator
    public ScanNode(
            @JsonProperty("name") String name,
            @JsonProperty("table") SchemaTable table,
            @JsonProperty("fields") Map<String, Type> fields,
            @JsonProperty("invalidations") Map<String, Invalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, LinkageMask> linkageMasks,
            @JsonProperty("idNodes") Set<String> idNodes,
            @JsonProperty("lockOverride") Optional<LockOverride> lockOverride)
    {
        super(name, invalidations, linkageMasks, lockOverride);

        this.table = checkNotNull(table);
        this.fields = ImmutableMap.copyOf(fields);
        this.idNodes = ImmutableSet.copyOf(idNodes);

        checkInvariants();
    }

    @JsonProperty("table")
    public SchemaTable getTable()
    {
        return table;
    }

    @JsonProperty("fields")
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @JsonProperty("idNodes")
    public Set<String> getIdNodes()
    {
        return idNodes;
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        throw new IllegalStateException();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitScanNode(this, context);
    }
}
