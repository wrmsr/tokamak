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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public interface Type
{
    PrimitiveType BOOLEAN = new PrimitiveType<>(Boolean.class);
    PrimitiveType LONG = new PrimitiveType<>(Long.class);
    PrimitiveType DOUBLE = new PrimitiveType<>(Double.class);
    PrimitiveType STRING = new PrimitiveType<>(String.class);
    PrimitiveType BYTES = new PrimitiveType<>(byte[].class);

    Map<Class<?>, Type> FROM_JAVA_TYPE = ImmutableMap.<Class<?>, Type>builder()
            .put(Boolean.class, BOOLEAN)
            .put(Long.class, LONG)
            .put(Double.class, DOUBLE)
            .put(String.class, STRING)
            .put(byte[].class, BYTES)
            .build();

    @JsonValue
    default String toRepr()
    {
        return TypeUtils.toRepr(this);
    }

    @JsonCreator
    static Type parseRepr(String str)
    {
        return TypeUtils.parseRepr(str);
    }
}
