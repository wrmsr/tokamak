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

import com.google.common.base.Preconditions;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.TypeLike;
import com.wrmsr.tokamak.util.Pair;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

public final class TypeUtils
{
    private TypeUtils()
    {
    }

    public static boolean isValidArg(Object a)
    {
        return a instanceof TypeLike || a instanceof Long;
    }

    public static <T> T checkValidArg(T a)
    {
        checkArgument(isValidArg(a));
        return a;
    }

    public static List<Type> objectsToTypes(List<Object> objects)
    {
        return objects.stream().map(Preconditions::checkNotNull).map(Type.class::cast).collect(toImmutableList());
    }

    public static Map<String, Type> objectsToTypes(Map<String, Object> objects)
    {
        return objects.entrySet().stream().map(e -> Pair.immutable(e.getKey(), (Type) checkNotNull(e.getValue()))).collect(toImmutableMap());
    }
}
