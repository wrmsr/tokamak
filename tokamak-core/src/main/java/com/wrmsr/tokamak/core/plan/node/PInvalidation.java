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

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PInvalidation
{
    public enum Strength
    {
        STRONG,
        WEAK,
    }

    private final String node;
    private final String field;
    private final Strength strength;

    @JsonCreator
    public PInvalidation(
            @JsonProperty("node") String node,
            @JsonProperty("field") String field,
            @JsonProperty("strength") Strength strength)
    {
        this.node = checkNotEmpty(node);
        this.field = checkNotEmpty(field);
        this.strength = checkNotNull(strength);
    }

    @Override
    public String toString()
    {
        return "Invalidation{" +
                "node=" + node +
                ", field=" + field +
                ", strength=" + strength +
                '}';
    }

    @JsonProperty("node")
    public String getNode()
    {
        return node;
    }

    @JsonProperty("fields")
    public String getField()
    {
        return field;
    }

    @JsonProperty("strength")
    public Strength getStrength()
    {
        return strength;
    }
}
