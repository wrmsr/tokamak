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
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PCrossJoin;
import com.wrmsr.tokamak.core.plan.node.PEquiJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroupBy;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PSingleSource;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.CachingPNodeVisitor;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitors;
import com.wrmsr.tokamak.util.collect.StreamableIterable;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMapMap;
import static com.wrmsr.tokamak.util.MoreCollections.sorted;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

@Immutable
public final class FieldOriginAnalysis
        implements StreamableIterable<FieldOriginAnalysis.Origination>
{
    /*
    TODO:
     - SUBFIELD TRACKING. INTO STRUCTS.
      - groupBy List<Struct<...>> ele origins?
    */

    public enum Strength
    {
        OUTER(false),
        INNER(false),

        SCAN(true),
        VALUES(true),
        GROUP(true),

        OPAQUE(true);

        private final boolean generated;

        Strength(boolean generated)
        {
            this.generated = generated;
        }

        public boolean isGenerated()
        {
            return generated;
        }
    }

    public interface Nesting
    {
        final class Nested
                implements Nesting
        {
            private final String sinkSubfield;

            private Nested(String sinkSubfield)
            {
                this.sinkSubfield = checkNotNull(sinkSubfield);
            }

            public String getSinkSubfield()
            {
                return sinkSubfield;
            }

            @Override
            public String toString()
            {
                return "Nested{" +
                        "sinkSubfield='" + sinkSubfield + '\'' +
                        '}';
            }
        }

        final class None
                implements Nesting
        {
            private static final None INSTANCE = new None();

            private None()
            {
            }

            @Override
            public String toString()
            {
                return "None{}";
            }
        }

        final class Unnested
                implements Nesting
        {
            private final String sourceSubfield;

            private Unnested(String sourceSubfield)
            {
                this.sourceSubfield = checkNotNull(sourceSubfield);
            }

            public String getSourceSubfield()
            {
                return sourceSubfield;
            }

            @Override
            public String toString()
            {
                return "Unnested{" +
                        "sourceSubfield='" + sourceSubfield + '\'' +
                        '}';
            }
        }

        static Nested nested(String subfield)
        {
            return new Nested(subfield);
        }

        static None none()
        {
            return None.INSTANCE;
        }

        static Unnested unnested(String subfield)
        {
            return new Unnested(subfield);
        }
    }

    @Immutable
    public static final class Origination
    {
        private final PNodeField sink;
        private final Optional<PNodeField> source;
        private final Strength strength;
        private final Nesting nesting;

        private Origination(PNodeField sink, Optional<PNodeField> source, Strength strength, Nesting nesting)
        {
            this.sink = checkNotNull(sink);
            this.source = checkNotNull(source);
            this.strength = checkNotNull(strength);
            this.nesting = checkNotNull(nesting);
            if (source.isPresent()) {
                checkState(sink.getNode().getSources().contains(source.get().getNode()));
                checkArgument(!strength.generated);
            }
            else {
                checkArgument(strength.generated);
                checkArgument(nesting instanceof Nesting.None);
            }
        }

        private Origination(PNodeField sink, PNodeField source, Strength strength, Nesting nesting)
        {
            this(sink, Optional.of(source), strength, nesting);
        }

        private Origination(PNodeField sink, Strength strength)
        {
            this(sink, Optional.empty(), strength, Nesting.none());
        }

        @Override
        public String toString()
        {
            return "Origination{" +
                    "sink=" + sink +
                    ", source=" + source +
                    ", strength=" + strength +
                    ", nesting=" + nesting +
                    '}';
        }

        public PNodeField getSink()
        {
            return sink;
        }

        public Optional<PNodeField> getSource()
        {
            return source;
        }

        public Strength getStrength()
        {
            return strength;
        }
    }

    public static final class MissingOriginationsException
            extends RuntimeException
    {
        private final PNode node;
        private final Set<String> presentFields;

        private final Set<String> missingFields;

        public MissingOriginationsException(PNode node, Set<String> presentFields)
        {
            this.node = checkNotNull(node);
            this.presentFields = checkNotEmpty(ImmutableSet.copyOf(presentFields));

            missingFields = checkNotEmpty(ImmutableSet.copyOf(Sets.difference(node.getFields().getNames(), this.presentFields)));
        }

        @Override
        public String toString()
        {
            return "MissingOriginationsException{" +
                    "node=" + node +
                    ", presentFields=" + presentFields +
                    ", missingFields=" + missingFields +
                    '}';
        }

        public PNode getNode()
        {
            return node;
        }

        public Set<String> getPresentFields()
        {
            return presentFields;
        }

        public Set<String> getMissingFields()
        {
            return missingFields;
        }
    }

    private final List<Origination> originations;
    private final Map<PNode, Integer> toposortIndicesByNode;

    private final Map<PNodeField, Set<Origination>> originationSetsBySink;
    private final Map<PNodeField, Set<Origination>> originationSetsBySource;

    private final Map<PNode, Map<String, Set<Origination>>> sinkOriginationSetsByNodeByField;
    private final Map<PNode, Map<String, Set<Origination>>> sourceOriginationSetsByNodeByField;

    private FieldOriginAnalysis(List<Origination> originations, Map<PNode, Integer> toposortIndicesByNode)
    {
        this.originations = ImmutableList.copyOf(originations);
        this.toposortIndicesByNode = ImmutableMap.copyOf(toposortIndicesByNode);

        Map<PNodeField, Set<Origination>> originationSetsBySink = new LinkedHashMap<>();
        Map<PNodeField, Set<Origination>> originationSetsBySource = new LinkedHashMap<>();

        Map<PNode, Map<String, Set<Origination>>> sinkOriginationSetsByNodeByField = new LinkedHashMap<>();
        Map<PNode, Map<String, Set<Origination>>> sourceOriginationSetsByNodeByField = new LinkedHashMap<>();

        this.originations.forEach(o -> {
            checkState(toposortIndicesByNode.containsKey(o.sink.getNode()));
            originationSetsBySink
                    .computeIfAbsent(o.sink, nf -> new LinkedHashSet<>())
                    .add(o);
            sinkOriginationSetsByNodeByField
                    .computeIfAbsent(o.sink.getNode(), n -> new LinkedHashMap<>())
                    .computeIfAbsent(o.sink.getField(), f -> new LinkedHashSet<>())
                    .add(o);
            o.source.ifPresent(src -> {
                checkState(toposortIndicesByNode.containsKey(src.getNode()));
                checkState(toposortIndicesByNode.get(src.getNode()) < toposortIndicesByNode.get(o.sink.getNode()));
                originationSetsBySource
                        .computeIfAbsent(src, nf -> new LinkedHashSet<>())
                        .add(o);
                sourceOriginationSetsByNodeByField
                        .computeIfAbsent(src.getNode(), n -> new LinkedHashMap<>())
                        .computeIfAbsent(src.getField(), f -> new LinkedHashSet<>())
                        .add(o);
            });
        });

        this.originationSetsBySink = newImmutableSetMap(originationSetsBySink);
        this.originationSetsBySource = newImmutableSetMap(originationSetsBySource);

        this.sinkOriginationSetsByNodeByField = newImmutableSetMapMap(sinkOriginationSetsByNodeByField);
        this.sourceOriginationSetsByNodeByField = newImmutableSetMapMap(sourceOriginationSetsByNodeByField);

        sinkOriginationSetsByNodeByField.forEach((snkNode, snkOrisByField) -> {
            Set<String> missingFields = Sets.difference(snkNode.getFields().getNames(), snkOrisByField.keySet());
            if (!missingFields.isEmpty()) {
                throw new MissingOriginationsException(snkNode, snkOrisByField.keySet());
            }

            snkOrisByField.forEach((snkField, snkOris) -> {
                checkNotEmpty(snkOris);
                if (snkOris.stream().anyMatch(o -> o.strength.generated)) {
                    checkSingle(snkOris);
                }
            });
        });
    }

    public List<Origination> getOriginations()
    {
        return originations;
    }

    public Map<PNodeField, Set<Origination>> getOriginationSetsBySink()
    {
        return originationSetsBySink;
    }

    public Map<PNodeField, Set<Origination>> getOriginationSetsBySource()
    {
        return originationSetsBySource;
    }

    public Map<PNode, Map<String, Set<Origination>>> getSinkOriginationSetsByNodeByField()
    {
        return sinkOriginationSetsByNodeByField;
    }

    public Map<PNode, Map<String, Set<Origination>>> getSourceOriginationSetsByNodeByField()
    {
        return sourceOriginationSetsByNodeByField;
    }

    private final SupplierLazyValue<Map<PNodeField, Set<Origination>>> leafOriginationSetsBySink = new SupplierLazyValue<>();

    public Map<PNodeField, Set<Origination>> getLeafOriginationSetsBySink()
    {
        return leafOriginationSetsBySink.get(() -> {
            Map<PNodeField, Set<Origination>> leafOriginationSetsBySink = new LinkedHashMap<>();

            sorted(sinkOriginationSetsByNodeByField.keySet(), Comparator.comparing(toposortIndicesByNode::get)).forEach(snkNode -> {
                sinkOriginationSetsByNodeByField.get(snkNode).forEach((snkField, snkOris) -> {
                    checkNotEmpty(snkOris);
                    Set<Origination> snkLeafOriginationSet;
                    if (snkOris.stream().anyMatch(o -> o.strength.generated)) {
                        snkLeafOriginationSet = ImmutableSet.of(checkSingle(snkOris));
                    }
                    else {
                        snkLeafOriginationSet = new LinkedHashSet<>();
                        snkOris.forEach(snkOri -> {
                            if (snkOri.source.isPresent()) {
                                PNodeField srcNf = snkOri.source.get();
                                Set<Origination> srcLeafOriginationSet = checkNotNull(leafOriginationSetsBySink.get(srcNf));
                                snkLeafOriginationSet.addAll(checkNotEmpty(srcLeafOriginationSet));
                            }
                            else {
                                snkLeafOriginationSet.add(snkOri);
                            }
                        });
                    }
                    PNodeField snkNf = PNodeField.of(snkNode, snkField);
                    checkState(!leafOriginationSetsBySink.containsKey(snkNf));
                    leafOriginationSetsBySink.put(snkNf, checkNotEmpty(ImmutableSet.copyOf(snkLeafOriginationSet)));
                });
            });

            return newImmutableSetMap(leafOriginationSetsBySink);
        });
    }

    @Override
    public Iterator<Origination> iterator()
    {
        return originations.iterator();
    }

    public static FieldOriginAnalysis analyze(Plan plan)
    {
        List<Origination> originations = new ArrayList<>();

        PNodeVisitors.postWalk(plan.getRoot(), new CachingPNodeVisitor<Void, Void>()
        {
            private void addSimpleSingleSource(PSingleSource node)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(PNodeField.of(node, f), PNodeField.of(node.getSource(), f), Strength.INNER, Nesting.none())));
            }

            private void visitSources(PNode node, Void context)
            {
                node.getSources().forEach(s -> process(s, context));
            }

            @Override
            public Void visitCache(PCache node, Void context)
            {
                addSimpleSingleSource(node);

                return null;
            }

            @Override
            public Void visitCrossJoin(PCrossJoin node, Void context)
            {
                Strength str = node.getMode() == PCrossJoin.Mode.FULL ? Strength.INNER : Strength.OUTER;
                node.getSources().forEach(s ->
                        s.getFields().getNames().forEach(f -> originations.add(new Origination(
                                PNodeField.of(node, f), PNodeField.of(s, f), str, Nesting.none()))));

                return null;
            }

            @Override
            public Void visitEquiJoin(PEquiJoin node, Void context)
            {
                node.getBranches().forEach(b -> {
                    Strength str =
                            ((node.getMode() == PEquiJoin.Mode.LEFT && b == node.getBranches().get(0)) || node.getMode() == PEquiJoin.Mode.FULL) ?
                                    Strength.INNER : Strength.OUTER;
                    b.getNode().getFields().getNames().forEach(f -> {
                        originations.add(new Origination(PNodeField.of(node, f), PNodeField.of(b.getNode(), f), str, Nesting.none()));
                    });
                    node.getBranches().forEach(ob -> {
                        checkState(ob.getFields().size() == b.getFields().size());
                        for (int i = 0; i < b.getFields().size(); ++i) {
                            String kf = b.getFields().get(i);
                            String okf = ob.getFields().get(i);
                            originations.add(new Origination(PNodeField.of(node, kf), PNodeField.of(ob.getNode(), okf), str, Nesting.none()));
                        }
                    });
                });

                return null;
            }

            @Override
            public Void visitFilter(PFilter node, Void context)
            {
                addSimpleSingleSource(node);

                return null;
            }

            @Override
            public Void visitGroupBy(PGroupBy node, Void context)
            {
                originations.add(new Origination(
                        PNodeField.of(node, node.getListField()), Strength.GROUP));
                node.getGroupFields().forEach(gf -> originations.add(new Origination(
                        PNodeField.of(node, gf), PNodeField.of(node.getSource(), gf), Strength.INNER, Nesting.none())));

                return null;
            }

            @Override
            public Void visitLookupJoin(PLookupJoin node, Void context)
            {
                node.getSource().getFields().getNames().forEach(f -> originations.add(new Origination(
                        PNodeField.of(node, f), PNodeField.of(node.getSource(), f), Strength.INNER, Nesting.none())));
                node.getBranches().forEach(b -> b.getFields().forEach(f -> originations.add(new Origination(
                        PNodeField.of(node, f), PNodeField.of(b.getNode(), f), Strength.OUTER, Nesting.none()))));

                return null;
            }

            @Override
            public Void visitProject(PProject node, Void context)
            {
                node.getProjection().getInputsByOutput().forEach((o, i) -> {
                    if (i instanceof PProjection.FieldInput) {
                        PProjection.FieldInput fi = (PProjection.FieldInput) i;
                        originations.add(new Origination(PNodeField.of(node, o), PNodeField.of(node.getSource(), fi.getField()), Strength.INNER, Nesting.none()));
                    }
                    else {
                        originations.add(new Origination(PNodeField.of(node, o), Strength.OPAQUE));
                    }
                });

                return null;
            }

            @Override
            public Void visitScan(PScan node, Void context)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(PNodeField.of(node, f), Strength.SCAN)));

                return null;
            }

            @Override
            public Void visitState(PState node, Void context)
            {
                addSimpleSingleSource(node);

                return null;
            }

            @Override
            public Void visitStruct(PStruct node, Void context)
            {
                // FIXME:
                checkState(false);

                return null;
            }

            @Override
            public Void visitUnion(PUnion node, Void context)
            {
                // FIXME:
                checkState(false);

                return null;
            }

            @Override
            public Void visitUnnest(PUnnest node, Void context)
            {
                // FIXME:
                checkState(false);

                return null;
            }

            @Override
            public Void visitValues(PValues node, Void context)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(PNodeField.of(node, f), Strength.VALUES)));

                return null;
            }
        }, null);

        return new FieldOriginAnalysis(originations, plan.getToposortIndicesByNode());
    }
}
