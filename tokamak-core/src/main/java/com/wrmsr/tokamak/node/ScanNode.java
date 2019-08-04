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
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.serdes.MapSerializer;
import com.wrmsr.tokamak.serdes.Serializer;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

@Immutable
public final class ScanNode
        extends StatefulNode
        implements GeneratorNode
{
    private final String table;
    private final Map<String, Type> fields;

    public ScanNode(
            String name,
            String table,
            Map<String, Type> fields,
            Map<String, Invalidation> invalidations,
            Map<String, LinkageMask> linkageMasks,
            List<String> idNodes)
    {
        super(name, invalidations, linkageMasks);

        this.table = table;
        this.fields = ImmutableMap.copyOf(fields);

        checkInvariants();
    }

    public String getTable()
    {
        return table;
    }

    public Map<String, Type> getFields()
    {
        return fields;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitScanNode(this, context);
    }

    public static final Serializer<ScanNode> SERIALIZER = MapSerializer.of(
            (ctx, node) -> {
                return ImmutableMap.<String, Object>builder()
                        .put("name", node.getName())
                        .put("table", node.getTable())
                        .build();
            },
            (ctx, map) -> {
                return new ScanNode(
                        (String) map.get("name"),
                        (String) map.get("table"),
                )
            }
    );
}
