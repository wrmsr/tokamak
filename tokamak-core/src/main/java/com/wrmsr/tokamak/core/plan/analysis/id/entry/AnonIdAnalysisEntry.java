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
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.plan.analysis.id.part.IdAnalysisPart;
import com.wrmsr.tokamak.core.plan.node.PNode;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Set;

@Immutable
public final class AnonIdAnalysisEntry
        extends IdAnalysisEntry
{
    private final Set<AnonIdAnalysisEntry> dependencies;

    AnonIdAnalysisEntry(PNode node, Set<AnonIdAnalysisEntry> dependencies)
    {
        super(node);

        this.dependencies = ImmutableSet.copyOf(dependencies);
    }

    @Override
    public List<IdAnalysisPart> getParts()
    {
        return ImmutableList.of();
    }

    public Set<AnonIdAnalysisEntry> getDependencies()
    {
        return dependencies;
    }
}
