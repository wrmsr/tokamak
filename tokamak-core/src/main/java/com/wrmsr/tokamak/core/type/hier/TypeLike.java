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
package com.wrmsr.tokamak.core.type.hier;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.type.TypeRendering;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

@Immutable
public interface TypeLike
{
    String getName();

    default List<Object> getTypeArgs()
    {
        return ImmutableList.of();
    }

    default Map<String, Object> getTypeKwargs()
    {
        return ImmutableMap.of();
    }

    @JsonValue
    default String toSpec()
    {
        return TypeRendering.buildSpec((Object) this);
    }
}
