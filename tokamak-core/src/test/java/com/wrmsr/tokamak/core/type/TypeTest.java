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

import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.annotation.NotNullType;
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

        System.out.println(Types.BUILTIN_REGISTRY.fromSpec("List<Long>"));

        Type type = Types.BUILTIN_REGISTRY.fromSpec("Internal<NotNull<Map<Long, Sized<NotNull<Long>, 420>>>>");
        System.out.println(type);
        System.out.println(type.toSpec());

        type = Types.BUILTIN_REGISTRY.fromSpec("Struct<x=NotNull<Long>, y=Internal<Long>>");
        System.out.println(type);
        System.out.println(type.toSpec());
    }

    public void testSigils()
            throws Throwable
    {
        Type type;

        type = Types.Internal(Types.NotNull(Types.Map(Types.Sized(16, Types.Long()), Types.String())));
        System.out.println(type.toSpec());
        Types.BUILTIN_REGISTRY.fromSpec(type.toSpec());
        // type.desigil();

        // type = Types.NotNull(Types.NotNull((Types.Long())));
        // type.desigil();

        type = TypeAnnotations.drop(type, NotNullType.class);
        System.out.println(type.toSpec());
    }

    public void testJson()
            throws Throwable
    {
        System.out.println(Json.writeValue(Types.List(Types.Long())));
    }
}
