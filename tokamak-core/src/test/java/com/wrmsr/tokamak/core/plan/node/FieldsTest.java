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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.FieldAnnotation;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

import java.util.List;

public class FieldsTest
        extends TestCase
{
    public void testFieldsJson()
            throws Throwable
    {
        Field fld = new Field("x", Types.LONG, ImmutableList.of(FieldAnnotation.notNull()));

        String json = Json.writeValue(fld);

        System.out.println(json);

        Field fld2 = Json.readValue(json, Field.class);

        System.out.println(fld2);
    }

    public void testFieldAnnsJson()
            throws Throwable
    {
        TypeReference<List<FieldAnnotation>> tr = new TypeReference<List<FieldAnnotation>>() {};

        List<FieldAnnotation> anns = ImmutableList.of(
                FieldAnnotation.id(),
                FieldAnnotation.internal(),
                FieldAnnotation.notNull()
        );

        System.out.println(anns);

        String json = Json.mapper().writerFor(tr).writeValueAsString(anns);

        System.out.println(json);

        List<FieldAnnotation> anns2 = Json.readValue(json, tr);

        System.out.println(anns2);
    }
}
