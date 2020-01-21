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
package com.wrmsr.tokamak.core.plan.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PFunction
{
    private final String name;
    private final FunctionType type;

    @JsonCreator
    public PFunction(
            @JsonProperty("name") String name,
            @JsonProperty("type") FunctionType type)
    {
        this.name = checkNotEmpty(name);
        this.type = checkNotNull(type);
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("type")
    public FunctionType getType()
    {
        return type;
    }

    public static PFunction of(Executable exe)
    {
        return new PFunction(exe.getName(), exe.getType());
    }
}
