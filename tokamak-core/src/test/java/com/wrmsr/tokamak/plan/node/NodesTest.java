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
package com.wrmsr.tokamak.plan.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.NodeId;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.exec.Reflection;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.json.Json;
import org.junit.Test;

import java.util.Optional;

public class NodesTest
{
    @Test
    public void testJsonStuff()
            throws Throwable
    {
        Object obj = Json.OBJECT_MAPPER_SUPPLIER.get().readValue("{\"a\": [420]}", Object.class);

        String s = Json.writeValue(NodeId.of("hi there"));
        System.out.println(s);
        NodeId nid = Json.readValue(s, NodeId.class);
        System.out.println(nid);

        s = Json.writeValue(Id.of(new byte[] {(byte) 0x12, (byte) 0x14}));
        System.out.println(s);
        Id id = Json.readValue(s, Id.class);
        System.out.println(id);
    }

    @Test
    public void testNodes()
            throws Throwable
    {
        Node scanNode = new ScanNode(
                "scan0",
                SchemaTable.of("public", "hi"),
                ImmutableMap.of("id", Type.LONG, "thing", Type.STRING),
                ImmutableSet.of("id"),
                ImmutableSet.of(),
                ImmutableMap.of(),
                ImmutableMap.of(),
                Optional.empty());

        Node projectNode = new ProjectNode(
                "project0",
                scanNode,
                Projection.of(
                        "id", "id",
                        "fn", Reflection.reflect(() -> 0L)
                ));

        String json = Json.writeValuePretty(projectNode);
        System.out.println(json);

        Node deserNode = Json.readValue(json, Node.class);
        System.out.println(deserNode);
    }
}
