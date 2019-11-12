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

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    }

    @Immutable
    public static final class NodeEntry
    {
        private final List<Invalidation> invalidations;
        private final Optional<Set<String>> updateMask;
    }

    private final Map<PNodeId, NodeEntry> entriesByNode;
}
