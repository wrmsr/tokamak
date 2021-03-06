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
package com.wrmsr.tokamak.core.catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.exec.Purity;
import com.wrmsr.tokamak.core.plan.node.PFunction;
import com.wrmsr.tokamak.core.type.hier.special.FunctionType;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class Function
{
    private final String name;
    private final FunctionType type;
    private final Purity purity;
    private final Executor executor;

    private Catalog catalog;

    public Function(
            Catalog catalog,
            String name,
            FunctionType type,
            Purity purity,
            Executor executor)
    {
        this.catalog = checkNotNull(catalog);
        this.name = checkNotEmpty(name);
        this.type = checkNotNull(type);
        this.purity = checkNotNull(purity);
        this.executor = checkNotNull(executor);
    }

    @JsonCreator
    private Function(
            @JsonProperty("name") String name,
            @JsonProperty("type") FunctionType type,
            @JsonProperty("purity") Purity purity,
            @JsonProperty("executor") Executor executor)
    {
        this.name = checkNotNull(name);
        this.type = checkNotNull(type);
        this.purity = checkNotNull(purity);
        this.executor = checkNotNull(executor);
    }

    void setCatalog(Catalog catalog)
    {
        checkState(this.catalog == null);
        this.catalog = checkNotNull(catalog);
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

    @JsonProperty("executor")
    public Executor getExecutor()
    {
        return executor;
    }

    public Catalog getCatalog()
    {
        return catalog;
    }

    private final SupplierLazyValue<Executable> executable = new SupplierLazyValue<>();

    public Executable getExecutable()
    {
        return executable.get(() -> executor.getExecutable(name));
    }

    private final SupplierLazyValue<PFunction> nodeFunction = new SupplierLazyValue<>();

    public PFunction asNodeFunction()
    {
        return nodeFunction.get(() -> new PFunction(name, type, purity));
    }
}
