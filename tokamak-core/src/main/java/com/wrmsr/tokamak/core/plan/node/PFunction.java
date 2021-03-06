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
import com.wrmsr.tokamak.core.exec.Purity;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PFunction
{
    private final String name;
    private final FunctionType type;
    private final Purity purity;

    @JsonCreator
    public PFunction(
            @JsonProperty("name") String name,
            @JsonProperty("type") FunctionType type,
            @JsonProperty("purity") Purity purity)
    {
        this.name = checkNotEmpty(name);
        this.type = checkNotNull(type);
        this.purity = checkNotNull(purity);
        if (purity == Purity.IDENTITY) {
            checkArgument(type.getParams().size() == 1);
        }
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

    @JsonProperty("purity")
    public Purity getPurity()
    {
        return purity;
    }

    public static PFunction of(Executable exe)
    {
        return new PFunction(exe.getName(), exe.getType(), exe.getPurity());
    }
}
