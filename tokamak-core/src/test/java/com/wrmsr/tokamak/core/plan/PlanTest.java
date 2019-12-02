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
package com.wrmsr.tokamak.core.plan;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;
import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

public class PlanTest
        extends TestCase
{
    public void testAnns()
            throws Throwable
    {
        AnnotationCollectionMap<String, FieldAnnotation> anns = AnnotationCollectionMap.copyOf(
                ImmutableMap.of("x", AnnotationCollection.of(FieldAnnotation.id())));

        String src = Json.writeValue(anns);

        System.out.println(src);
    }
}
