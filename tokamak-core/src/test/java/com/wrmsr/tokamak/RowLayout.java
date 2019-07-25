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
package com.wrmsr.tokamak;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public final class RowLayout
{
    private final List<String> fields;
    private final Map<String, Type> typesByField;
    private final Map<String, Integer> positionsByField;

    public RowLayout(Map<String, Type> typesByField)
    {
        this.typesByField = ImmutableMap.copyOf(typesByField);
        fields = ImmutableList.copyOf(typesByField.keySet());
        positionsByField = IntStream.range(0, fields.size()).boxed().collect(toImmutableMap(fields::get, identity()));
    }

    @Override
    public String toString()
    {
        return "RowLayout{" +
                "typesByField=" + typesByField +
                '}';
    }

    public List<String> getFields()
    {
        return fields;
    }

    public Map<String, Type> getTypesByField()
    {
        return typesByField;
    }

    public Map<String, Integer> getPositionsByField()
    {
        return positionsByField;
    }
}
