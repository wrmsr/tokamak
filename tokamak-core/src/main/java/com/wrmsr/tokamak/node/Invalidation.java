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
package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

@Immutable
public final class Invalidation
{
    private final Set<String> fields;
    private final boolean isSoft;

    @JsonCreator
    public Invalidation(
            @JsonProperty("fields") Set<String> fields,
            @JsonProperty("isSoft") boolean isSoft)
    {
        this.fields = ImmutableSet.copyOf(fields);
        this.isSoft = isSoft;
    }

    @Override
    public String toString()
    {
        return "Invalidation{" +
                "fields=" + fields +
                ", isSoft=" + isSoft +
                '}';
    }

    @JsonProperty("fields")
    public Set<String> getFields()
    {
        return fields;
    }

    @JsonProperty("isSoft")
    public boolean isSoft()
    {
        return isSoft;
    }
}
