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
package com.wrmsr.tokamak.api;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Objects;

@Immutable
public final class Attributes
{
    private final Map<FieldName, Object> map;

    public Attributes(Map<FieldName, Object> map)
    {
        this.map = ImmutableMap.copyOf(map);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Attributes that = (Attributes) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(map);
    }

    public Map<FieldName, Object> getMap()
    {
        return map;
    }
}
