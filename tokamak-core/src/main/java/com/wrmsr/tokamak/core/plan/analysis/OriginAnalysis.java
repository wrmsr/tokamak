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
import com.wrmsr.tokamak.core.plan.node.PExtract;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PProjection;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PScope;
import com.wrmsr.tokamak.core.plan.node.PScopeExit;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.PSingleSource;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnify;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.node.visitor.CachingPNodeVisitor;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitors;
import com.wrmsr.tokamak.util.Pair;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;
import static com.wrmsr.tokamak.util.MoreCollections.sorted;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

@Immutable
public final class OriginAnalysis
        implements StreamableIterable<OriginAnalysis.Origination>
{
    /*
    TODO:
     - SUBFIELD TRACKING. INTO STRUCTS.
      - groupBy List<Struct<...>> ele origins?
    */

    public enum Genesis
    {
        DIRECT(false),

        INNER_JOIN(false),
        LEFT_JOIN_PRIMARY(false),
        LEFT_JOIN_SECONDARY(false),
        FULL_JOIN(false),
        LOOKUP_JOIN(false),

        SCAN(true),
        VALUES(true),

        GROUP(true),

        OPAQUE(true);

        private final boolean leaf;

        Genesis(boolean leaf)
        {
            this.leaf = leaf;
        }

        public boolean isLeaf()
        {
            return leaf;
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
        private final Genesis genesis;
        private final Nesting nesting;

        private Origination(PNodeField sink, Optional<PNodeField> source, Genesis genesis, Nesting nesting)
        {
            this.sink = checkNotNull(sink);
            this.source = checkNotNull(source);
            this.genesis = checkNotNull(genesis);
            this.nesting = checkNotNull(nesting);
            if (source.isPresent()) {
                PNodeField src = source.get();
                checkState(src != sink);
                checkState(sink.getNode().getSources().contains(src.getNode()));
                checkArgument(!genesis.leaf);
            }
            else {
                checkArgument(genesis.leaf);
                checkArgument(nesting instanceof Nesting.None);
            }
        }

        private Origination(PNodeField sink, PNodeField source, Genesis genesis, Nesting nesting)
        {
            this(sink, Optional.of(source), genesis, nesting);
        }

        private Origination(PNodeField sink, Genesis genesis)
        {
            this(sink, Optional.empty(), genesis, Nesting.none());
        }

        @Override
        public String toString()
        {
            return "Origination{" +
                    "sink=" + sink +
                    ", source=" + source +
                    ", genesis=" + genesis +
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

        public Genesis getGenesis()
        {
            return genesis;
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

    private final Map<PNode, Map<String, Set<Origination>>> originationSetsBySinkNodeBySinkField;
    private final Map<PNode, Map<String, Set<Origination>>> originationSetsBySourceNodeBySourceField;

    private OriginAnalysis(List<Origination> originations, Map<PNode, Integer> toposortIndicesByNode)
    {
        this.originations = ImmutableList.copyOf(originations);
        this.toposortIndicesByNode = ImmutableMap.copyOf(toposortIndicesByNode);

        Map<PNodeField, Set<Origination>> originationSetsBySink = new LinkedHashMap<>();
        Map<PNodeField, Set<Origination>> originationSetsBySource = new LinkedHashMap<>();
        Set<Pair<PNodeField, Optional<PNodeField>>> seenPairs = new LinkedHashSet<>();

        this.originations.forEach(o -> {
            Pair<PNodeField, Optional<PNodeField>> pair = Pair.immutable(o.sink, o.source);
            checkState(!seenPairs.contains(pair));
            seenPairs.add(pair);

            checkState(toposortIndicesByNode.containsKey(o.sink.getNode()));
            originationSetsBySink
                    .computeIfAbsent(o.sink, nf -> new LinkedHashSet<>())
                    .add(o);

            o.source.ifPresent(src -> {
                checkState(toposortIndicesByNode.containsKey(src.getNode()));
                checkState(toposortIndicesByNode.get(src.getNode()) < toposortIndicesByNode.get(o.sink.getNode()));
                originationSetsBySource
                        .computeIfAbsent(src, nf -> new LinkedHashSet<>())
                        .add(o);
            });
        });

        this.originationSetsBySink = newImmutableSetMap(originationSetsBySink);
        this.originationSetsBySource = newImmutableSetMap(originationSetsBySource);

        this.originationSetsBySinkNodeBySinkField = PNodeField.expandNodeFieldMap(originationSetsBySink);
        this.originationSetsBySourceNodeBySourceField = PNodeField.expandNodeFieldMap(originationSetsBySource);

        originationSetsBySinkNodeBySinkField.forEach((snkNode, snkOrisByField) -> {
            Set<String> missingFields = Sets.difference(snkNode.getFields().getNames(), snkOrisByField.keySet());
            if (!missingFields.isEmpty()) {
                throw new MissingOriginationsException(snkNode, snkOrisByField.keySet());
            }

            snkOrisByField.forEach((snkField, snkOris) -> {
                checkNotEmpty(snkOris);
                if (snkOris.stream().anyMatch(o -> o.genesis.leaf)) {
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

    public Map<PNode, Map<String, Set<Origination>>> getOriginationSetsBySinkNodeBySinkField()
    {
        return originationSetsBySinkNodeBySinkField;
    }

    public Map<PNode, Map<String, Set<Origination>>> getOriginationSetsBySourceNodeBySourceField()
    {
        return originationSetsBySourceNodeBySourceField;
    }

    @Immutable
    public static final class OriginationLink
    {
        private final Origination sink;
        private final Set<OriginationLink> next;

        private OriginationLink(Origination sink, Set<OriginationLink> next)
        {
            this.sink = checkNotNull(sink);
            checkArgument(next instanceof ImmutableSet);
            this.next = next;
        }

        @Override
        public String toString()
        {
            return "OriginationLink{" +
                    "sink=" + sink +
                    '}';
        }

        public Origination getSink()
        {
            return sink;
        }

        public Set<OriginationLink> getNext()
        {
            return next;
        }

        public void traverse(Consumer<Origination> consumer)
        {
            consumer.accept(sink);
            next.forEach(l -> l.traverse(consumer));
        }
    }

    @Immutable
    public final class ChainAnalysis
    {
        private final Predicate<Origination> splitPredicate;

        private final Set<Origination> firstOriginations;
        private final Map<PNodeField, Set<Origination>> firstOriginationSetsBySink;
        private final Map<Origination, Set<Origination>> firstOriginationSetsByOrigination;
        private final Map<PNodeField, Set<OriginationLink>> originationLinkSetsBySink;

        private ChainAnalysis(Predicate<Origination> splitPredicate)
        {
            this.splitPredicate = checkNotNull(splitPredicate);

            Set<Origination> firstOriginations = new LinkedHashSet<>();
            Map<PNodeField, Set<Origination>> firstOriginationSetsBySink = new LinkedHashMap<>();
            Map<Origination, Set<Origination>> firstOriginationSetsByOrigination = new LinkedHashMap<>();
            Map<PNodeField, Set<OriginationLink>> originationLinkSetsBySink = new LinkedHashMap<>();

            sorted(originationSetsBySinkNodeBySinkField.keySet(), Comparator.comparing(toposortIndicesByNode::get)).forEach(snkNode -> {
                originationSetsBySinkNodeBySinkField.get(snkNode).forEach((snkField, snkOris) -> {
                    checkNotEmpty(snkOris);

                    Set<Origination> snkFirstOriginationSet;
                    Set<OriginationLink> originationLinkSet;
                    if (snkOris.stream().anyMatch(o -> o.genesis.leaf)) {
                        Origination snkOri = checkSingle(snkOris);
                        firstOriginations.add(snkOri);
                        snkFirstOriginationSet = ImmutableSet.of(snkOri);
                        originationLinkSet = ImmutableSet.of(new OriginationLink(snkOri, ImmutableSet.of()));
                    }
                    else {
                        snkFirstOriginationSet = new LinkedHashSet<>();
                        originationLinkSet = new LinkedHashSet<>();
                        snkOris.forEach(snkOri -> {
                            checkState(snkOri.source.isPresent());
                            checkState(!firstOriginationSetsByOrigination.containsKey(snkOri));
                            Set<Origination> snkFirstOriginations;
                            if (splitPredicate.test(snkOri)) {
                                firstOriginations.add(snkOri);
                                snkFirstOriginations = ImmutableSet.of(snkOri);
                                originationLinkSet.add(new OriginationLink(snkOri, ImmutableSet.of()));
                            }
                            else {
                                snkFirstOriginations = checkNotEmpty(firstOriginationSetsBySink.get(snkOri.source.get()));
                                originationLinkSet.add(new OriginationLink(snkOri, checkNotEmpty(originationLinkSetsBySink.get(snkOri.source.get()))));
                            }
                            snkFirstOriginationSet.addAll(snkFirstOriginations);
                            firstOriginationSetsByOrigination.put(snkOri, snkFirstOriginations);
                        });
                    }

                    PNodeField snkNf = PNodeField.of(snkNode, snkField);
                    checkState(!firstOriginationSetsBySink.containsKey(snkNf));
                    firstOriginationSetsBySink.put(snkNf, checkNotEmpty(ImmutableSet.copyOf(snkFirstOriginationSet)));
                    originationLinkSetsBySink.put(snkNf, ImmutableSet.copyOf(originationLinkSet));
                });
            });

            this.firstOriginations = ImmutableSet.copyOf(firstOriginations);
            this.firstOriginationSetsBySink = newImmutableSetMap(firstOriginationSetsBySink);
            this.firstOriginationSetsByOrigination = newImmutableSetMap(firstOriginationSetsByOrigination);
            this.originationLinkSetsBySink = ImmutableMap.copyOf(originationLinkSetsBySink);
        }

        public boolean shouldSplit(Origination ori)
        {
            return splitPredicate.test(checkNotNull(ori));
        }

        public Set<Origination> getFirstOriginations()
        {
            return firstOriginations;
        }

        public Map<PNodeField, Set<Origination>> getFirstOriginationSetsBySink()
        {
            return firstOriginationSetsBySink;
        }

        public Map<Origination, Set<Origination>> getFirstOriginationSetsByOrigination()
        {
            return firstOriginationSetsByOrigination;
        }

        public Map<PNodeField, Set<OriginationLink>> getOriginationLinkSetsBySink()
        {
            return originationLinkSetsBySink;
        }

        private final SupplierLazyValue<Map<PNodeField, Set<PNodeField>>> sinkSetsByFirstSource = new SupplierLazyValue<>();

        public Map<PNodeField, Set<PNodeField>> getSinkSetsByFirstSource()
        {
            return sinkSetsByFirstSource.get(() -> {
                Map<PNodeField, Set<PNodeField>> ret = new LinkedHashMap<>();
                firstOriginationSetsBySink.forEach((k, vs) -> {
                    vs.forEach(v -> {
                        checkState(!v.source.isPresent());
                        ret.computeIfAbsent(v.sink, v_ -> new LinkedHashSet<>()).add(k);
                    });
                });
                return newImmutableSetMap(ret);
            });
        }
    }

    public ChainAnalysis buildChainAnalysis(Predicate<Origination> splitPredicate)
    {
        return new ChainAnalysis(splitPredicate);
    }

    private final SupplierLazyValue<ChainAnalysis> leafChainAnalysis = new SupplierLazyValue<>();

    public ChainAnalysis getLeafChainAnalysis()
    {
        return leafChainAnalysis.get(() -> buildChainAnalysis(o -> false));
    }

    private final SupplierLazyValue<ChainAnalysis> stateChainAnalysis = new SupplierLazyValue<>();

    public ChainAnalysis getStateChainAnalysis()
    {
        return stateChainAnalysis.get(() -> buildChainAnalysis(o -> o.sink.getNode() instanceof PState));
    }

    @Override
    public Iterator<Origination> iterator()
    {
        return originations.iterator();
    }

    public static OriginAnalysis analyze(Plan plan)
    {
        List<Origination> originations = new ArrayList<>();

        PNodeVisitors.postWalk(plan.getRoot(), new CachingPNodeVisitor<Void, Void>()
        {
            private void addDirectSingleSource(PSingleSource node)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(
                                PNodeField.of(node, f), PNodeField.of(node.getSource(), f), Genesis.DIRECT, Nesting.none())));
            }

            @Override
            public Void visitCache(PCache node, Void context)
            {
                addDirectSingleSource(node);

                return null;
            }

            @Override
            public Void visitExtract(PExtract node, Void context)
            {
                // FIXME:
                checkState(false);

                return null;
            }

            @Override
            public Void visitFilter(PFilter node, Void context)
            {
                addDirectSingleSource(node);

                return null;
            }

            @Override
            public Void visitGroup(PGroup node, Void context)
            {
                originations.add(new Origination(
                        PNodeField.of(node, node.getListField()), Genesis.GROUP));
                node.getKeyFields().forEach(gf -> originations.add(new Origination(
                        PNodeField.of(node, gf), PNodeField.of(node.getSource(), gf), Genesis.DIRECT, Nesting.none())));

                return null;
            }

            @Override
            public Void visitJoin(PJoin node, Void context)
            {
                node.getBranches().forEach(b -> {
                    Genesis gen;
                    switch (node.getMode()) {
                        case INNER:
                            gen = Genesis.INNER_JOIN;
                            break;
                        case LEFT:
                            gen = b == node.getBranches().get(0) ? Genesis.LEFT_JOIN_PRIMARY : Genesis.LEFT_JOIN_SECONDARY;
                            break;
                        case FULL:
                            gen = Genesis.FULL_JOIN;
                            break;
                        default:
                            throw new IllegalStateException(Objects.toString(node.getMode()));
                    }

                    b.getNode().getFields().getNames().forEach(f -> {
                        originations.add(new Origination(PNodeField.of(node, f), PNodeField.of(b.getNode(), f), gen, Nesting.none()));
                    });

                    node.getBranches().forEach(ob -> {
                        if (ob != b) {
                            checkState(ob.getFields().size() == b.getFields().size());
                            for (int i = 0; i < b.getFields().size(); ++i) {
                                String kf = b.getFields().get(i);
                                String okf = ob.getFields().get(i);
                                originations.add(new Origination(PNodeField.of(node, kf), PNodeField.of(ob.getNode(), okf), gen, Nesting.none()));
                            }
                        }
                    });
                });

                return null;
            }

            @Override
            public Void visitLookupJoin(PLookupJoin node, Void context)
            {
                node.getSource().getFields().getNames().forEach(f -> originations.add(new Origination(
                        PNodeField.of(node, f), PNodeField.of(node.getSource(), f), Genesis.DIRECT, Nesting.none())));
                node.getBranches().forEach(b -> b.getFields().forEach(f -> originations.add(new Origination(
                        PNodeField.of(node, f), PNodeField.of(b.getNode(), f), Genesis.LOOKUP_JOIN, Nesting.none()))));

                return null;
            }

            @Override
            public Void visitOutput(POutput node, Void context)
            {
                addDirectSingleSource(node);

                return null;
            }

            @Override
            public Void visitProject(PProject node, Void context)
            {
                node.getProjection().getInputsByOutput().forEach((o, i) -> {
                    if (i instanceof PProjection.FieldInput) {
                        PProjection.FieldInput fi = (PProjection.FieldInput) i;
                        originations.add(new Origination(
                                PNodeField.of(node, o), PNodeField.of(node.getSource(), fi.getField()), Genesis.DIRECT, Nesting.none()));
                    }
                    else {
                        originations.add(new Origination(
                                PNodeField.of(node, o), Genesis.OPAQUE));
                    }
                });

                return null;
            }

            @Override
            public Void visitScan(PScan node, Void context)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(PNodeField.of(node, f), Genesis.SCAN)));

                return null;
            }

            @Override
            public Void visitScope(PScope node, Void context)
            {
                // FIXME:
                checkState(false);

                return null;
            }

            @Override
            public Void visitScopeExit(PScopeExit node, Void context)
            {
                // FIXME:
                checkState(false);

                return null;
            }

            @Override
            public Void visitSearch(PSearch node, Void context)
            {
                // FIXME:
                checkState(false);

                return null;
            }

            @Override
            public Void visitState(PState node, Void context)
            {
                addDirectSingleSource(node);

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
            public Void visitUnify(PUnify node, Void context)
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
                        originations.add(new Origination(PNodeField.of(node, f), Genesis.VALUES)));

                return null;
            }
        }, null);

        return new

                OriginAnalysis(originations, plan.getToposortIndicesByNode());
    }
}
