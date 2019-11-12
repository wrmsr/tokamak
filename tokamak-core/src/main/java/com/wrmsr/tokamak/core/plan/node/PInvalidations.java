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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class PInvalidations
{
    public enum Strength
    {
        STRONG,
        WEAK,
    }

    @Immutable
    public static final class Invalidation
    {
        private final Map<String, String> keyFieldsBySourceField;
        private final Optional<Set<String>> updateMask;
        private final Strength strength;

        @JsonCreator
        public Invalidation(
                @JsonProperty("keyFieldsBySourceField") Map<String, String> keyFieldsBySourceField,
                @JsonProperty("updateMask") Optional<Set<String>> updateMask,
                @JsonProperty("strength") Strength strength)
        {
            this.keyFieldsBySourceField = ImmutableMap.copyOf(keyFieldsBySourceField);
            this.updateMask = checkNotNull(updateMask).map(ImmutableSet::copyOf);
            this.strength = checkNotNull(strength);
        }

        @JsonProperty("keyFieldsBySourceField")
        public Map<String, String> getKeyFieldsBySourceField()
        {
            return keyFieldsBySourceField;
        }

        @JsonProperty("updateMask")
        public Optional<Set<String>> getUpdateMask()
        {
            return updateMask;
        }

        @JsonProperty("strength")
        public Strength getStrength()
        {
            return strength;
        }
    }

    @Immutable
    public static final class NodeEntry
    {
        private final List<Invalidation> invalidations;
        private final Optional<Set<String>> updateMask;

        @JsonCreator
        public NodeEntry(
                @JsonProperty("invalidations") List<Invalidation> invalidations,
                @JsonProperty("updateMask") Optional<Set<String>> updateMask)
        {
            this.invalidations = ImmutableList.copyOf(invalidations);
            this.updateMask = checkNotNull(updateMask).map(ImmutableSet::copyOf);
        }

        @JsonProperty("invalidations")
        public List<Invalidation> getInvalidations()
        {
            return invalidations;
        }

        @JsonProperty("updateMask")
        public Optional<Set<String>> getUpdateMask()
        {
            return updateMask;
        }
    }

    private final Map<String, NodeEntry> entriesByNode;

    @JsonCreator
    public PInvalidations(
            @JsonProperty("entriesByNode") Map<String, NodeEntry> entriesByNode)
    {
        this.entriesByNode = ImmutableMap.copyOf(entriesByNode);
    }

    @JsonProperty("entriesByNode")
    public Map<String, NodeEntry> getEntriesByNode()
    {
        return entriesByNode;
    }

    private static final PInvalidations EMPTY = new PInvalidations(ImmutableMap.of());

    public static PInvalidations empty()
    {
        return EMPTY;
    }
}
