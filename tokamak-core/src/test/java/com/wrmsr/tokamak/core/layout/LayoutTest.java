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
package com.wrmsr.tokamak.core.layout;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.type.Types;
import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

public class LayoutTest
        extends TestCase
{
    public void testFiedlCollecJson()
            throws Throwable
    {
        FieldCollection fc = FieldCollection.builder()
                .add(new Field("x", Types.Long(), ImmutableList.of(FieldAnnotation.id())))
                .add(new Field("y", Types.Long(), ImmutableList.of()))
                .build();
        String src = Json.writeValue(fc);
        System.out.println(src);
    }
}
