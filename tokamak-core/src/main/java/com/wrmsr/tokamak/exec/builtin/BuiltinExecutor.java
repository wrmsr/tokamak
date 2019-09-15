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
package com.wrmsr.tokamak.exec.builtin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrmsr.tokamak.catalog.Executor;
import com.wrmsr.tokamak.exec.Executable;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

public final class BuiltinExecutor
        implements Executor
{
    private final String name;

    private final Map<String, Executable> executablesByName = new HashMap<>();

    @JsonCreator
    public BuiltinExecutor(
            @JsonProperty("name") String name)
    {
        this.name = checkNotEmpty(name);
    }

    @JsonProperty("name")
    @Override
    public String getName()
    {
        return name;
    }

    public Executable register(Executable executable)
    {
        checkNotNull(executable);
        checkState(!executablesByName.containsKey(executable.getName()));
        executablesByName.put(executable.getName(), executable);
        return executable;
    }

    @Override
    public Executable getExecutable(String name)
    {
        return checkNotNull(executablesByName.get(name));
    }
}
