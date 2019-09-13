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

package com.wrmsr.tokamak.catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public final class Function
{
    private final String name;
    private final Executor executor;

    private Catalog catalog;

    private final Object lock = new Object();

    public Function(Catalog catalog, String name, Executor executor)
    {
        this.catalog = checkNotNull(catalog);
        this.name = checkNotEmpty(name);
        this.executor = checkNotNull(executor);
    }

    @JsonCreator
    private Function(
            @JsonProperty("name") String name,
            @JsonProperty("executor") Executor executor)
    {
        this.name = checkNotNull(name);
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

    @JsonProperty("executor")
    public Executor getExecutor()
    {
        return executor;
    }
}
