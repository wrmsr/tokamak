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
package com.wrmsr.tokamak.core.plan.node;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.SchemaTable;
import com.wrmsr.tokamak.core.exec.Reflection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.value.PValue;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.core.util.ApiJson;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.json.Json;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodesTest
{
    @BeforeClass
    public static void beforeClass()
            throws Throwable
    {
        ApiJson.installStatics();
    }

    @Test
    public void testJsonStuff()
            throws Throwable
    {
        Object obj = Json.OBJECT_MAPPER_SUPPLIER.get().readValue("{\"a\": [420]}", Object.class);

        String s = Json.writeValue(PNodeId.of("hi there"));
        System.out.println(s);
        PNodeId nid = Json.readValue(s, PNodeId.class);
        System.out.println(nid);

        s = Json.writeValue(Id.of(new byte[] {(byte) 0x12, (byte) 0x14}));
        System.out.println(s);
        Id id = Json.readValue(s, Id.class);
        System.out.println(id);
    }

    public static long zero()
    {
        return 0L;
    }

    @Test
    public void testNodes()
            throws Throwable
    {
        PNode scanNode = new PScan(
                "scan0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.copyOf(ImmutableMap.of("id", AnnotationCollection.of(FieldAnnotation.id()))),
                SchemaTable.of("public", "hi"),
                ImmutableMap.of("id", Types.Long(), "thing", Types.String()),
                PInvalidations.empty());

        String json = Json.writeValuePretty(scanNode);
        System.out.println(json);

        PNode deserNode = Json.readValue(json, PNode.class);
        System.out.println(deserNode);

        PNode projectNode = new PProject(
                "project0",
                AnnotationCollection.of(),
                AnnotationCollectionMap.of(),
                scanNode,
                new PProjection(ImmutableMap.of(
                        "id", PValue.field("id"),
                        "fn", PValue.function(PFunction.of(Reflection.reflect(NodesTest.class.getDeclaredMethod("zero"))))
                )));

        json = Json.writeValuePretty(projectNode);
        System.out.println(json);

        deserNode = Json.readValue(json, PNode.class);
        System.out.println(deserNode);
    }
}
