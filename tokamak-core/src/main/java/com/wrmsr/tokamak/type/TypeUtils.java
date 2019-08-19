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
package com.wrmsr.tokamak.type;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeUtils
{
    private TypeUtils()
    {
    }

    public static final BiMap<Type, String> PRIMITIVE_STRING_MAP = ImmutableBiMap.<Type, String>builder()
            .put(Type.BOOLEAN, "boolean")
            .put(Type.LONG, "long")
            .put(Type.DOUBLE, "double")
            .put(Type.BYTES, "bytes")
            .put(Type.STRING, "string")
            .build();

    public static Type parseRepr(String str)
    {
        return checkNotNull(PRIMITIVE_STRING_MAP.inverse().get(str));
    }

    public static String toRepr(Type type)
    {
        return PRIMITIVE_STRING_MAP.get(type);
    }
}
