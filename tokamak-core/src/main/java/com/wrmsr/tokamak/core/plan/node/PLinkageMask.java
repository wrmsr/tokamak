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
public final class PLinkageMask
{
    private final Set<String> fields;

    @JsonCreator
    public PLinkageMask(
            @JsonProperty("fields") Set<String> fields)
    {
        this.fields = checkNotEmpty(ImmutableSet.copyOf(fields));
    }

    @Override
    public String toString()
    {
        return "LinkageMask{" +
                "fields=" + fields +
                '}';
    }

    @JsonProperty("fields")
    public Set<String> getFields()
    {
        return fields;
    }
}