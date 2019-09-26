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
import com.wrmsr.tokamak.core.exec.Signature;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class Function
{
    private final String name;
    private final Signature signature;

    @JsonCreator
    public Function(
            @JsonProperty("name") String name,
            @JsonProperty("signature") Signature signature)
    {
        this.name = checkNotEmpty(name);
        this.signature = checkNotNull(signature);
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("signature")
    public Signature getSignature()
    {
        return signature;
    }

    public static Function of(Executable exe)
    {
        return new Function(exe.getName(), exe.getSignature());
    }
}