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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.plan.node.PNode;

import javax.annotation.concurrent.Immutable;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MorePreconditions.checkContains;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class BuildablePNode
        implements PNodeAnnotation
{
    private final Set<Set<String>> fieldSets;

    @JsonCreator
    public BuildablePNode(
            @JsonProperty("fieldSets") Set<Set<String>> fieldSets)
    {
        this.fieldSets = checkNotNull(fieldSets).stream().map(ImmutableSet::copyOf).collect(toImmutableSet());
        this.fieldSets.forEach(s -> checkNotEmpty(s).forEach(f -> checkNotEmpty(f)));
    }

    @JsonProperty("fieldSets")
    public Set<Set<String>> getFieldSets()
    {
        return fieldSets;
    }

    @Override
    public String toDisplayString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        return sb.toString();
    }

    public static void validate(PNode node)
    {
        BuildablePNode ann = node.getAnnotations().get(BuildablePNode.class).get();
        ann.fieldSets.forEach(s -> s.forEach(f -> checkContains(f, node.getFields().getNames())));
    }
}
