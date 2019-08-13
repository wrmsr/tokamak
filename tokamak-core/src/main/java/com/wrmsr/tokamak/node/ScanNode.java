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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.OrderPreservingImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;

@Immutable
public final class ScanNode
        extends StatefulNode
        implements GeneratorNode
{
    private final SchemaTable schemaTable;
    private final Map<String, Type> fields;
    private final Set<String> idFields;
    private final Set<String> idNodes;

    @JsonCreator
    public ScanNode(
            @JsonProperty("name") String name,
            @JsonProperty("schemaTable") SchemaTable schemaTable,
            @JsonProperty("fields") Map<String, Type> fields,
            @JsonProperty("idFields") Set<String> idFields,
            @JsonProperty("idNodes") Set<String> idNodes,
            @JsonProperty("invalidations") Map<String, Invalidation> invalidations,
            @JsonProperty("linkageMasks") Map<String, LinkageMask> linkageMasks,
            @JsonProperty("lockOverride") Optional<LockOverride> lockOverride)
    {
        super(name, invalidations, linkageMasks, lockOverride);

        this.schemaTable = checkNotNull(schemaTable);
        this.fields = ImmutableMap.copyOf(checkOrdered(fields));
        this.idFields = ImmutableSet.copyOf(idFields);
        this.idNodes = ImmutableSet.copyOf(idNodes);

        this.idFields.forEach(f -> checkState(this.fields.containsKey(f)));

        checkInvariants();
    }

    @JsonProperty("schemaTable")
    public SchemaTable getSchemaTable()
    {
        return schemaTable;
    }

    @JsonSerialize(using = OrderPreservingImmutableMap.Serializer.class)
    @JsonDeserialize(using = OrderPreservingImmutableMap.Deserializer.class)
    @JsonProperty("fields")
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @JsonProperty("idFields")
    public Set<String> getIdFields()
    {
        return idFields;
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
