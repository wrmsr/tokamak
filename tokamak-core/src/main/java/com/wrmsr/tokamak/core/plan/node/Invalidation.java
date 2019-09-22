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
import com.google.common.collect.ImmutableSet;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class Invalidation
{
    private final Set<String> fields;
    private final boolean soft;

    @JsonCreator
    public Invalidation(
            @JsonProperty("fields") Set<String> fields,
            @JsonProperty("soft") boolean soft)
    {
        this.fields = checkNotEmpty(ImmutableSet.copyOf(fields));
        this.soft = soft;
    }

    @Override
    public String toString()
    {
        return "Invalidation{" +
                "fields=" + fields +
                ", soft=" + soft +
                '}';
    }

    @JsonProperty("fields")
    public Set<String> getFields()
    {
        return fields;
    }

    @JsonProperty("soft")
    public boolean isSoft()
    {
        return soft;
    }
}
