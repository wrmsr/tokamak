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
package com.wrmsr.tokamak.core.plan.analysis.id.entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.plan.analysis.id.part.IdAnalysisPart;
import com.wrmsr.tokamak.core.plan.node.PNode;

import javax.annotation.concurrent.Immutable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class StandardIdAnalysisEntry
        extends IdAnalysisEntry
{
    private final List<IdAnalysisPart> parts;
    private final Map<String, Integer> positionsByField;

    StandardIdAnalysisEntry(PNode node, List<IdAnalysisPart> parts)
    {
        super(node);
        this.parts = checkNotEmpty(ImmutableList.copyOf(parts));

        Set<String> seen = new HashSet<>();
        ImmutableMap.Builder<String, Integer> positionsByField = ImmutableMap.builder();
        for (int i = 0; i < this.parts.size(); ++i) {
            for (String field : this.parts.get(i)) {
                checkState(!seen.contains(field));
                checkState(node.getFields().contains(field));
                seen.add(field);
                positionsByField.put(field, i);
            }
        }
        this.positionsByField = positionsByField.build();
    }

    @Override
    public List<IdAnalysisPart> getParts()
    {
        return parts;
    }

    public Map<String, Integer> getPositionsByField()
    {
        return positionsByField;
    }

    public int getPosition(String field)
    {
        return positionsByField.get(checkNotEmpty(field));
    }
}
