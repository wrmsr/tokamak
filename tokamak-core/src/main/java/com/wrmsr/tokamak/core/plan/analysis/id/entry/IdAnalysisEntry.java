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
import com.wrmsr.tokamak.core.layout.field.annotation.IdField;
import com.wrmsr.tokamak.core.plan.analysis.id.part.IdAnalysisPart;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Immutable
public abstract class IdAnalysisEntry
        implements StreamableIterable<IdAnalysisPart>
{
    protected final PNode node;

    protected IdAnalysisEntry(PNode node)
    {
        this.node = checkNotNull(node);
    }

    public PNode getNode()
    {
        return node;
    }

    protected void checkInvariants()
    {
        getParts().forEach(p -> p.forEach(f -> checkArgument(node.getFields().contains(f))));
    }

    public abstract List<IdAnalysisPart> getParts();

    @Override
    public Iterator<IdAnalysisPart> iterator()
    {
        return getParts().iterator();
    }

    public boolean contains(String field)
    {
        return stream().anyMatch(p -> p.contains(field));
    }

    public static IdAnalysisEntry anon(PNode node, Iterable<AnonIdAnalysisEntry> dependencies)
    {
        return new AnonIdAnalysisEntry(node, ImmutableSet.copyOf(dependencies));
    }

    public static IdAnalysisEntry anon(PNode node)
    {
        return new AnonIdAnalysisEntry(node, ImmutableSet.of());
    }

    public static IdAnalysisEntry inherit(PNode node, IdAnalysisEntry source)
    {
        return new InheritedIdAnalysisEntry(node, source);
    }

    public static IdAnalysisEntry of(PNode node, Iterable<IdAnalysisPart> parts)
    {
        List<IdAnalysisPart> list = ImmutableList.copyOf(parts);
        if (list.isEmpty()) {
            return new AnonIdAnalysisEntry(node, ImmutableSet.of());
        }
        else {
            return new StandardIdAnalysisEntry(node, list);
        }
    }

    public static IdAnalysisEntry fromAnnotations(PNode node)
    {
        return of(
                node,
                node.getFieldAnnotations().getKeySetsByAnnotationCls().getOrDefault(IdField.class, ImmutableSet.of()).stream()
                        .map(IdAnalysisPart::of)
                        .collect(toImmutableList()));
    }

    public static IdAnalysisEntry unify(PNode node, Iterable<IdAnalysisEntry> entries, Iterable<IdAnalysisPart> extras)
    {
        List<IdAnalysisEntry> entriesList = ImmutableList.copyOf(entries);
        List<IdAnalysisPart> extrasList = ImmutableList.copyOf(extras);
        Set<AnonIdAnalysisEntry> anonEntries = entriesList.stream()
                .filter(AnonIdAnalysisEntry.class::isInstance)
                .map(AnonIdAnalysisEntry.class::cast)
                .collect(toImmutableSet());
        if (!anonEntries.isEmpty()) {
            return anon(node, anonEntries);
        }
        List<IdAnalysisPart> parts = new ArrayList<>();
        for (IdAnalysisEntry entry : entriesList) {
            checkState(!(entry instanceof AnonIdAnalysisEntry));
            parts.addAll(entry.getParts());
        }
        parts.addAll(extrasList);
        return of(node, IdAnalysisPart.unify(parts));
    }

    public static IdAnalysisEntry unify(PNode node, Iterable<IdAnalysisEntry> entries)
    {
        return unify(node, entries, ImmutableList.of());
    }
}
