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
package com.wrmsr.tokamak.core.type;

import com.wrmsr.tokamak.core.type.impl.ListType;
import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

public class TypeTest
        extends TestCase
{
    public void testParsing()
            throws Throwable
    {
        for (String str : new String[] {
                "Long",
                "List<Long>",
                "Map<Long, String>",
                "Enum<x=0>",
                "Struct<x=Long, y=Double>",
                "Barf<0, Long, x=1, y=Double>",
        }) {
            System.out.println(TypeParsing.parseType(str));
        }
    }

    public void testJson()
            throws Throwable
    {
        System.out.println(Json.writeValue(new ListType(Types.LONG)));
    }
}
