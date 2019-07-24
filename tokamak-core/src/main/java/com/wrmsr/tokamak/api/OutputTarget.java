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

@Immutable
public abstract class OutputTarget
{
    private final OutputName name;
    private final Map<String, Object> options;

    public OutputTarget(OutputName name, Map<String, Object> options)
    {
        this.name = name;
        this.options = ImmutableMap.copyOf(options);
    }

    public OutputName getName()
    {
        return name;
    }

    public Map<String, Object> getOptions()
    {
        return options;
    }
}
