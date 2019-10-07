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
package com.wrmsr.tokamak.core.plan.analysis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class IdAnalysis
{
    @Immutable
    public static final class Entry
            implements StreamableIterable<Set<String>>
    {
        private final PNode node;
        private final Set<Set<String>> sets;

        public Entry(PNode node, Set<Set<String>> sets)
        {
            this.node = checkNotNull(node);
            this.sets = sets.stream().map(ImmutableSet::copyOf).collect(toImmutableSet());
            this.sets.forEach(s -> {
                checkNotEmpty(s);
                s.forEach(f -> checkState(node.getFields().contains(f)));
            });
        }

        public Entry(PNode node, Entry source)
        {
            this.node = checkNotNull(node);
            sets = source.getSets();
        }

        public PNode getNode()
        {
            return node;
        }

        public Set<Set<String>> getSets()
        {
            return sets;
        }

        public boolean isEmpty()
        {
            return sets.isEmpty();
        }

        @Override
        public Iterator<Set<String>> iterator()
        {
            return sets.iterator();
        }
    }

    private final Map<PNode, Entry> entriesByNode;

    private IdAnalysis(Map<PNode, Entry> entriesByNode)
    {
        this.entriesByNode = ImmutableMap.copyOf(entriesByNode);
        this.entriesByNode.forEach((k, v) -> checkState(k == v.getNode()));
    }

    public static IdAnalysis analyze(Plan plan)
    {
        Map<PNode, Entry> entriesByNode = new HashMap<>();

        /*
        PNodeVisitors.cacheAll(plan.getRoot(), new PNodeVisitor<Entry, Void>()
        {
            @Override
            public Entry visitCacheNode(PCache node, Void context)
            {
                throw new IllegalStateException();
            }

            @Override
            public Entry visitCrossJoinNode(PCrossJoin node, Void context)
            {
                // checkSingle(this.sources.stream().map(Node::getIdFieldSets).map(Set::size).collect(toImmutableSet()));
                // this.idFieldSets = Sets.cartesianProduct(this.sources.stream().map(Node::getIdFieldSets).collect(toImmutableList())).stream()
                //         .map(l -> l.stream().flatMap(Collection::stream).collect(toImmutableSet()))
                //         .collect(toImmutableSet());
                throw new IllegalStateException();
            }

            @Override
            public Entry visitEquijoinNode(PEquiJoin node, Void context)
            {
                // branchSetsByIdFieldSet = node.getBranches().stream()
                //         .flatMap(b -> b.getNode().getIdFieldSets().stream().map(fs -> Pair.immutable(fs, b)))
                //         .collect(groupingBySet(Pair::first)).entrySet().stream()
                //         .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().map(Pair::second).collect(toImmutableSet())));
                // idFieldSets = Sets.cartesianProduct(ImmutableList.copyOf(branchSetsByIdFieldSet.keySet())).stream()
                //         .map(l -> ImmutableSet.<String>builder().addAll(l).build()).collect(toImmutableSet());
                throw new IllegalStateException();
            }

            @Override
            public Entry visitFilterNode(PFilter node, Void context)
            {
                return new Entry(node, get(node.getSource(), context));
            }

            @Override
            public Entry visitGroupByNode(PGroupBy node, Void context)
            {
                return new Entry(node, ImmutableSet.of(ImmutableSet.of(node.getGroupField())));
            }

            @Override
            public Entry visitLookupJoinNode(PLookupJoin node, Void context)
            {
                return new Entry(node, get(node.getSource(), context));
            }

            @Override
            public Entry visitPersistNode(PState node, Void context)
            {
                return new Entry(node, get(node.getSource(), context));
            }

            @Override
            public Entry visitProjectNode(PProject node, Void context)
            {
                Entry source = get(node.getSource(), context);

                ImmutableSet.Builder<Set<String>> builder = ImmutableSet.builder();
                for (Set<String> set : source.getSets()) {
                    List<Set<String>> outputSets = set.stream()
                            .map(f -> node.getProjection().getOutputSetsByInputField().getOrDefault(f, ImmutableSet.of()))
                            .collect(toImmutableList());
                    if (outputSets.stream().anyMatch(Set::isEmpty)) {
                        continue;
                    }
                    builder.addAll(Sets.cartesianProduct(outputSets).stream()
                            .map(ImmutableSet::copyOf)
                            .collect(toImmutableList()));
                }

                return new Entry(node, builder.build());
            }

            @Override
            public Entry visitScanNode(PScan node, Void context)
            {
                return new Entry(node, ImmutableSet.of(node.getIdFields()));
            }

            @Override
            public Entry visitUnionNode(PUnion node, Void context)
            {
                if (node.getIndexField().isPresent() && node.getSources().stream().noneMatch(s -> get(s, context).isEmpty())) {
                    List<Set<Set<String>>> sourceSets = node.getSources().stream()
                            .map(s -> get(s, context).getSets())
                            .collect(toImmutableList());
                    Set<Set<String>> sets = Sets.cartesianProduct(sourceSets).stream()
                            .map(ss -> ImmutableSet.<String>builder()
                                    .addAll(ss.stream().flatMap(Set::stream).collect(toImmutableSet()))
                                    .add(node.getIndexField().get())
                                    .build())
                            .collect(toImmutableSet());
                    return new Entry(node, sets);
                }
                else {
                    return new Entry(node, ImmutableSet.of());
                }
            }

            @Override
            public Entry visitUnnestNode(PUnnest node, Void context)
            {
                if (node.getIndexField().isPresent()) {
                    Set<Set<String>> sets = get(node.getSource(), context).getSets().stream()
                            .map(s -> ImmutableSet.<String>builder().addAll(s).add(node.getIndexField().get()).build())
                            .collect(toImmutableSet());
                    return new Entry(node, sets);
                }
                else {
                    return new Entry(node, ImmutableSet.of());
                }
            }

            @Override
            public Entry visitValuesNode(PValues node, Void context)
            {
                if (node.getIndexField().isPresent()) {
                    return new Entry(node, ImmutableSet.of(ImmutableSet.of(node.getIndexField().get())));
                }
                else {
                    return new Entry(node, ImmutableSet.of(ImmutableSet.of()));
                }
            }
        }, null);

        return new IdFieldAnalysis(entriesByNode);
        */

        throw new IllegalStateException();
    }
}
