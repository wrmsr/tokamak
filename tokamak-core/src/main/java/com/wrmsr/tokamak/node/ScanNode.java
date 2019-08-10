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
import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Immutable
public final class ScanNode
        extends StatefulNode
        implements GeneratorNode
{
    private final SchemaTable schemaTable;
    private final Map<String, Type> fields;
    private final Set<Set<String>> idFieldSets;
    private final Set<String> idNodes;

    @JsonCreator
    public ScanNode(
            @JsonProperty("name") String name,
            @JsonProperty("schemaTable") SchemaTable schemaTable,
            @JsonProperty("fields") Map<String, Type> fields,
            @JsonProperty("idFieldSets") Set<Set<String>> idFieldSets,
            @JsonProperty("idNodes") Set<String> idNodes,
            @JsonProperty("invalidations") Map<String, Invalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, LinkageMask> linkageMasks,
            @JsonProperty("lockOverride") Optional<LockOverride> lockOverride)
    {
        super(name, invalidations, linkageMasks, lockOverride);

        this.schemaTable = checkNotNull(schemaTable);
        this.fields = ImmutableMap.copyOf(fields);
        this.idFieldSets = idFieldSets.stream().map(ImmutableSet::copyOf).collect(toImmutableSet());
        this.idNodes = ImmutableSet.copyOf(idNodes);

        checkInvariants();
    }

    @JsonProperty("schemaTable")
    public SchemaTable getSchemaTable()
    {
        return schemaTable;
    }

    @JsonProperty("fields")
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @JsonProperty("idFieldSets")
    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        return idFieldSets;
    }

    @JsonProperty("idNodes")
    public Set<String> getIdNodes()
    {
        return idNodes;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitScanNode(this, context);
    }
}
