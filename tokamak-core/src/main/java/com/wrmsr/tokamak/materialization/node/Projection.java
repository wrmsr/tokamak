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
package com.wrmsr.tokamak.materialization.node;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.util.Box;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.function.Function;

@Immutable
public final class Projection
{
    public interface Input
    {
    }

    @Immutable
    public static final class FieldInput
            extends Box<FieldName>
            implements Input
    {
        public FieldInput(FieldName value)
        {
            super(value);
        }
    }

    @Immutable
    public static final class FunctionInput
            extends Box<Function>
            implements Input
    {
        public FunctionInput(Function value)
        {
            super(value);
        }
    }

    private final Map<FieldName, Input> map;

    public Projection(Map<FieldName, Input> map)
    {
        this.map = ImmutableMap.copyOf(map);
    }

    public Map<FieldName, Input> getMap()
    {
        return map;
    }
}
