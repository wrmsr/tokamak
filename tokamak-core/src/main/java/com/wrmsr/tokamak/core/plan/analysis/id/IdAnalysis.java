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
package com.wrmsr.tokamak.core.plan.analysis.id;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.catalog.Catalog;
import com.wrmsr.tokamak.core.catalog.Table;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.analysis.id.entry.IdAnalysisEntry;
import com.wrmsr.tokamak.core.plan.analysis.id.part.IdAnalysisPart;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PLookup;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PSingleSource;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.CachingPNodeVisitor;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitors;

import javax.annotation.concurrent.Immutable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Immutable
public final class IdAnalysis
{
    /*
    TODO:
     - left join nullness, 'preferred' parts?
      - tacked onto FieldPart?
     - enforce node fieldcoll order as it is significant
      - complicates inheritance?
     - optionally honor existing atts?
    */

    private final Map<PNode, IdAnalysisEntry> entriesByNode;

    private IdAnalysis(Map<PNode, IdAnalysisEntry> entriesByNode)
    {
        this.entriesByNode = ImmutableMap.copyOf(entriesByNode);
        this.entriesByNode.forEach((k, v) -> checkState(k == v.getNode()));
    }

    public Map<PNode, IdAnalysisEntry> getEntriesByNode()
    {
        return entriesByNode;
    }

    public IdAnalysisEntry get(PNode node)
    {
        return checkNotNull(entriesByNode.get(node));
    }

    public static IdAnalysis analyze(Plan plan, Optional<Catalog> catalog)
    {
        Map<PNode, IdAnalysisEntry> entriesByNode = new LinkedHashMap<>();

        PNodeVisitors.postWalk(plan.getRoot(), new CachingPNodeVisitor<IdAnalysisEntry, Void>(entriesByNode)
        {
            private IdAnalysisEntry inherit(PSingleSource node, Void context)
            {
                return IdAnalysisEntry.inherit(node, process(node.getSource(), context));
            }

            @Override
            public IdAnalysisEntry visitCache(PCache node, Void context)
            {
                return inherit(node, context);
            }

            @Override
            public IdAnalysisEntry visitFilter(PFilter node, Void context)
            {
                return inherit(node, context);
            }

            @Override
            public IdAnalysisEntry visitGroup(PGroup node, Void context)
            {
                return IdAnalysisEntry.of(node, node.getKeyFields().stream().map(IdAnalysisPart::of).collect(toImmutableList()));
            }

            @Override
            public IdAnalysisEntry visitJoin(PJoin node, Void context)
            {
                // FIXME: non-inner joins have nullable rest ids :|
                return IdAnalysisEntry.unify(
                        node,
                        node.getBranches().stream()
                                .map(b -> process(b.getNode(), context))
                                .collect(toImmutableList()));
            }

            @Override
            public IdAnalysisEntry visitLookup(PLookup node, Void context)
            {
                return IdAnalysisEntry.inherit(node, process(node.getSource(), context));
            }

            @Override
            public IdAnalysisEntry visitOutput(POutput node, Void context)
            {
                return inherit(node, context);
            }

            @Override
            public IdAnalysisEntry visitProject(PProject node, Void context)
            {
                IdAnalysisEntry source = process(node.getSource(), context);

                List<Set<String>> sets = source.getParts().stream()
                        .map(p -> p.getFields().stream()
                                .map(node.getProjection().getOutputSetsByInputField()::get)
                                .filter(Objects::nonNull)
                                .flatMap(Set::stream)
                                .collect(toImmutableSet()))
                        .collect(toImmutableList());

                if (sets.stream().noneMatch(Set::isEmpty)) {
                    return IdAnalysisEntry.of(node, sets.stream().map(IdAnalysisPart::of).collect(toImmutableList()));
                }
                else {
                    return IdAnalysisEntry.anon(node);
                }
            }

            @Override
            public IdAnalysisEntry visitScan(PScan node, Void context)
            {
                if (!catalog.isPresent()) {
                    return IdAnalysisEntry.anon(node);
                }

                Table table = catalog.get().getSchemaTable(node.getSchemaTable());
                if (table.getLayout().getPrimaryKeyFields().isEmpty() ||
                        !node.getFields().getNames().containsAll(table.getLayout().getPrimaryKeyFields())) {
                    return IdAnalysisEntry.anon(node);
                }


                return IdAnalysisEntry.of(node, table.getLayout().getPrimaryKeyFields().stream().map(IdAnalysisPart::of).collect(toImmutableList()));
            }

            @Override
            public IdAnalysisEntry visitState(PState node, Void context)
            {
                return inherit(node, context);
            }

            @Override
            public IdAnalysisEntry visitUnion(PUnion node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public IdAnalysisEntry visitUnnest(PUnnest node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public IdAnalysisEntry visitValues(PValues node, Void context)
            {
                throw new IllegalStateException();
            }
        }, null);

        return new IdAnalysis(entriesByNode);
    }
}
