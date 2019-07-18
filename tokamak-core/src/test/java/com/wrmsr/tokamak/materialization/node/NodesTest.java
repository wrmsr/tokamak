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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.Id;
import com.wrmsr.tokamak.materialization.api.NodeId;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.api.TableName;
import com.wrmsr.tokamak.materialization.type.Type;
import com.wrmsr.tokamak.util.Json;
import org.junit.Test;

import java.util.Optional;

public class NodesTest
{
    @Test
    public void testJsonStuff()
            throws Throwable
    {
        Object obj = Json.OBJECT_MAPPER_SUPPLIER.get().readValue("{\"a\": [420]}", Object.class);

        String s = Json.toJson(NodeId.compute(NodeName.of("hi there")));
        System.out.println(s);
        NodeId nid = Json.OBJECT_MAPPER_THREAD_LOCAL.get().readValue(s, NodeId.class);
        System.out.println(nid);

        s = Json.toJson(Id.of(new byte[]{ (byte) 0x12, (byte) 0x14 }));
        System.out.println(s);
        Id id = Json.OBJECT_MAPPER_THREAD_LOCAL.get().readValue(s, Id.class);
        System.out.println(id);
    }

    @Test
    public void testNodes()
            throws Throwable
    {
        Node node = new ScanNode(
                NodeName.of("hi"),
                TableName.of("hi"),
                ImmutableMap.of(FieldName.of("id"), Type.LONG, FieldName.of("thing"), Type.STRING),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        System.out.println(Json.toJson(node));
    }
}
