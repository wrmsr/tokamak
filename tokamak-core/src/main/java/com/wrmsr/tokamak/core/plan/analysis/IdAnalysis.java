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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.core.layout.field.annotation.IdField;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PCrossJoin;
import com.wrmsr.tokamak.core.plan.node.PEquiJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PSingleSource;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.CachingPNodeVisitor;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitors;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

@Immutable
public final class IdAnalysis
{
    /*
    TODO:
     - unify sets
     - 'unify' intrinsic - coalesce + assert all non-null equal, internal panic if not, idanalysis visible
    */

    @Immutable
    public static abstract class Part
            implements StreamableIterable<String>
    {
        public static Part of(Iterable<String> fields)
        {
            Set<String> set = ImmutableSet.copyOf(fields);
            if (set.size() == 1) {
                return new FieldPart(checkSingle(set));
            }
            else if (set.size() > 1) {
                return new SetPart(ImmutableSet.copyOf(set));
            }
            else {
                throw new IllegalArgumentException(Objects.toString(set));
            }
        }

        public static Part of(String... fields)
        {
            return of(ImmutableList.copyOf(fields));
        }
    }

    @Immutable
    public static final class FieldPart
            extends Part
    {
        private final String field;

        private FieldPart(String field)
        {
            this.field = checkNotEmpty(field);
        }

        @Override
        public String toString()
        {
            return "FieldPart{" +
                    "field='" + field + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            FieldPart fieldPart = (FieldPart) o;
            return Objects.equals(field, fieldPart.field);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(field);
        }

        public String getField()
        {
            return field;
        }

        @Override
        public Iterator<String> iterator()
        {
            return Iterators.singletonIterator(field);
        }
    }

    @Immutable
    public static final class SetPart
            extends Part
            implements StreamableIterable<String>
    {
        private final Set<String> fields;

        private SetPart(Set<String> fields)
        {
            this.fields = ImmutableSet.copyOf(fields);
            checkArgument(this.fields.size() > 1);
        }

        @Override
        public String toString()
        {
            return "SetPart{" +
                    "fields=" + fields +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            SetPart strings = (SetPart) o;
            return Objects.equals(fields, strings.fields);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(fields);
        }

        public Set<String> getFields()
        {
            return fields;
        }

        @Override
        public Iterator<String> iterator()
        {
            return fields.iterator();
        }
    }

    @Immutable
    public static abstract class Entry
            implements StreamableIterable<Part>
    {
        protected final PNode node;

        protected Entry(PNode node)
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

        public abstract List<Part> getParts();

        @Override
        public Iterator<Part> iterator()
        {
            return getParts().iterator();
        }

        public static Entry inherit(PSingleSource node, Entry source)
        {
            return new InheritedEntry(node, source);
        }

        public static Entry of(PNode node, Iterable<Part> parts)
        {
            List<Part> list = ImmutableList.copyOf(parts);
            if (list.isEmpty()) {
                return new AnonEntry(node);
            }
            else {
                return new StandardEntry(node, list);
            }
        }

        public static Entry fromAnnotations(PNode node)
        {
            return of(
                    node,
                    node.getAnnotations().getFields().getEntryListsByAnnotationCls().get(IdField.class).stream()
                            .map(e -> Part.of(e.getKey()))
                            .collect(toImmutableList()));
        }
    }

    @Immutable
    public static final class AnonEntry
            extends Entry
    {
        private AnonEntry(PNode node)
        {
            super(node);
        }

        @Override
        public List<Part> getParts()
        {
            return ImmutableList.of();
        }
    }

    @Immutable
    public static final class InheritedEntry
            extends Entry
    {
        private final Entry sourceEntry;
        private final List<Part> parts;

        private InheritedEntry(PNode node, Entry sourceEntry)
        {
            super(node);
            this.sourceEntry = checkNotNull(sourceEntry);
            parts = checkNotNull(sourceEntry.getParts());
        }

        public Entry getSourceEntry()
        {
            return sourceEntry;
        }

        @Override
        public List<Part> getParts()
        {
            return parts;
        }
    }

    @Immutable
    public static final class StandardEntry
            extends Entry
    {
        private final List<Part> parts;

        private StandardEntry(PNode node, List<Part> parts)
        {
            super(node);
            this.parts = checkNotEmpty(ImmutableList.copyOf(parts));
        }

        @Override
        public List<Part> getParts()
        {
            return parts;
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
        Map<PNode, Entry> entriesByNode = new LinkedHashMap<>();

        PNodeVisitors.postWalk(plan.getRoot(), new CachingPNodeVisitor<Entry, Void>(entriesByNode)
        {
            private Entry inherit(PSingleSource node, Void context)
            {
                return Entry.inherit(node, process(node.getSource(), context));
            }

            @Override
            public Entry visitCache(PCache node, Void context)
            {
                return inherit(node, context);
            }

            @Override
            public Entry visitCrossJoin(PCrossJoin node, Void context)
            {
                // checkSingle(this.sources.stream().map(Node::getIdFieldSets).map(Set::size).collect(toImmutableSet()));
                // this.idFieldSets = Sets.cartesianProduct(this.sources.stream().map(Node::getIdFieldSets).collect(toImmutableList())).stream()
                //         .map(l -> l.stream().flatMap(Collection::stream).collect(toImmutableSet()))
                //         .collect(toImmutableSet());
                throw new IllegalStateException();
            }

            @Override
            public Entry visitEquiJoin(PEquiJoin node, Void context)
            {
                // FIXME: ordered concatenation of all branch id fields MERGING ANY IN THE KEY
                // branchSetsByIdFieldSet = node.getBranches().stream()
                //         .flatMap(b -> b.getNode().getIdFieldSets().stream().map(fs -> Pair.immutable(fs, b)))
                //         .collect(groupingBySet(Pair::first)).entrySet().stream()
                //         .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().map(Pair::second).collect(toImmutableSet())));
                // idFieldSets = Sets.cartesianProduct(ImmutableList.copyOf(branchSetsByIdFieldSet.keySet())).stream()
                //         .map(l -> ImmutableSet.<String>builder().addAll(l).build()).collect(toImmutableSet());
                throw new IllegalStateException();
            }

            @Override
            public Entry visitFilter(PFilter node, Void context)
            {
                return inherit(node, context);
            }

            @Override
            public Entry visitGroup(PGroup node, Void context)
            {
                return new StandardEntry(node, node.getKeyFields().stream().map(Part::of).collect(toImmutableList()));
            }

            @Override
            public Entry visitLookupJoin(PLookupJoin node, Void context)
            {
                return new InheritedEntry(node, process(node.getSource(), context));
            }

            @Override
            public Entry visitProject(PProject node, Void context)
            {
                Entry source = process(node.getSource(), context);

                // ImmutableSet.Builder<Set<String>> builder = ImmutableSet.builder();
                // for (Set<String> set : source.getSets()) {
                //     List<Set<String>> outputSets = set.stream()
                //             .map(f -> node.getProjection().getOutputSetsByInputField().getOrDefault(f, ImmutableSet.of()))
                //             .collect(toImmutableList());
                //     if (outputSets.stream().anyMatch(Set::isEmpty)) {
                //         continue;
                //     }
                //     builder.addAll(Sets.cartesianProduct(outputSets).stream()
                //             .map(ImmutableSet::copyOf)
                //             .collect(toImmutableList()));
                // }
                //
                // return new Entry(node, builder.build());
                throw new IllegalStateException();
            }

            @Override
            public Entry visitScan(PScan node, Void context)
            {
                return Entry.fromAnnotations(node);
            }

            @Override
            public Entry visitState(PState node, Void context)
            {
                return inherit(node, context);
            }

            @Override
            public Entry visitUnion(PUnion node, Void context)
            {
                if (node.getIndexField().isPresent()) {
                    return
                }
                // if (node.getIndexField().isPresent() && node.getSources().stream().noneMatch(s -> process(s, context).isEmpty())) {
                //     List<Set<Set<String>>> sourceSets = node.getSources().stream()
                //             .map(s -> process(s, context).getSets())
                //             .collect(toImmutableList());
                //     Set<Set<String>> sets = Sets.cartesianProduct(sourceSets).stream()
                //             .map(ss -> ImmutableSet.<String>builder()
                //                     .addAll(ss.stream().flatMap(Set::stream).collect(toImmutableSet()))
                //                     .add(node.getIndexField().get())
                //                     .build())
                //             .collect(toImmutableSet());
                //     return new Entry(node, sets);
                // }
                // else {
                //     return new Entry(node, ImmutableSet.of());
                // }
                throw new IllegalStateException();
            }

            @Override
            public Entry visitUnnest(PUnnest node, Void context)
            {
                // if (node.getIndexField().isPresent()) {
                //     Set<Set<String>> sets = process(node.getSource(), context).getSets().stream()
                //             .map(s -> ImmutableSet.<String>builder().addAll(s).add(node.getIndexField().get()).build())
                //             .collect(toImmutableSet());
                //     return new Entry(node, sets);
                // }
                // else {
                //     return new Entry(node, ImmutableSet.of());
                // }
                throw new IllegalStateException();
            }

            @Override
            public Entry visitValues(PValues node, Void context)
            {
                // if (node.getIndexField().isPresent()) {
                //     return new Entry(node, ImmutableSet.of(ImmutableSet.of(node.getIndexField().get())));
                // }
                // else {
                //     return new Entry(node, ImmutableSet.of(ImmutableSet.of()));
                // }
                throw new IllegalStateException();
            }
        }, null);

        return new IdAnalysis(entriesByNode);
    }
}
