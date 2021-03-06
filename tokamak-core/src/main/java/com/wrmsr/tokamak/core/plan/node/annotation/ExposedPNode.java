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
package com.wrmsr.tokamak.core.plan.node.annotation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ExposedPNode
        implements PNodeAnnotation
{
    private final Optional<String> name;

    @JsonCreator
    public ExposedPNode(
            @JsonProperty("name") Optional<String> name)
    {
        this.name = checkNotNull(name);
    }

    @JsonProperty("name")
    public Optional<String> getName()
    {
        return name;
    }

    @Override
    public String toDisplayString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        name.ifPresent(name -> sb.append(": " + name));
        return sb.toString();
    }
}
