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
package com.wrmsr.tokamak.core.plan.analysis.origin;

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
import com.wrmsr.tokamak.core.plan.node.PLookup;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
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
import com.wrmsr.tokamak.core.plan.value.VConstant;
import com.wrmsr.tokamak.core.plan.value.VField;
import com.wrmsr.tokamak.core.plan.value.VFunction;
import com.wrmsr.tokamak.core.plan.value.VNode;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.collect.StreamableIterable;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

@Immutable
public final class OriginAnalysis
        implements StreamableIterable<Origination>
{
    final List<Origination> originations;
    final Map<PNode, Integer> toposortIndicesByNode;

    final Map<PNodeField, Set<Origination>> originationSetsBySink;
    final Map<PNodeField, Set<Origination>> originationSetsBySource;

    final Map<PNode, Map<String, Set<Origination>>> originationSetsBySinkNodeBySinkField;
    final Map<PNode, Map<String, Set<Origination>>> originationSetsBySourceNodeBySourceField;

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
                if (snkOris.stream().anyMatch(o -> o.geneses.stream().anyMatch(OriginGenesis::isLeaf))) {
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

    public OriginChainAnalysis buildChainAnalysis(Predicate<Origination> splitPredicate)
    {
        return new OriginChainAnalysis(this, splitPredicate);
    }

    private final SupplierLazyValue<OriginChainAnalysis> leafChainAnalysis = new SupplierLazyValue<>();

    public OriginChainAnalysis getLeafChainAnalysis()
    {
        return leafChainAnalysis.get(() -> buildChainAnalysis(o -> false));
    }

    private final SupplierLazyValue<OriginChainAnalysis> stateChainAnalysis = new SupplierLazyValue<>();

    public OriginChainAnalysis getStateChainAnalysis()
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
                                PNodeField.of(node, f), PNodeField.of(node.getSource(), f), ImmutableSet.of(OriginGenesis.direct()))));
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
                        PNodeField.of(node, node.getListField()), OriginGenesis.group()));
                node.getKeyFields().forEach(gf -> originations.add(new Origination(
                        PNodeField.of(node, gf), PNodeField.of(node.getSource(), gf), ImmutableSet.of(OriginGenesis.direct()))));

                return null;
            }

            @Override
            public Void visitJoin(PJoin node, Void context)
            {
                node.getBranches().forEach(b -> {
                    OriginGenesis gen;
                    switch (node.getMode()) {
                        case INNER:
                            gen = OriginGenesis.join(OriginGenesis.Join.Mode.INNER);
                            break;
                        case LEFT:
                            gen = b == node.getBranches().get(0) ? OriginGenesis.join(OriginGenesis.Join.Mode.LEFT_PRIMARY) : OriginGenesis.join(OriginGenesis.Join.Mode.LEFT_SECONDARY);
                            break;
                        case FULL:
                            gen = OriginGenesis.join(OriginGenesis.Join.Mode.FULL);
                            break;
                        default:
                            throw new IllegalStateException(Objects.toString(node.getMode()));
                    }

                    b.getNode().getFields().getNames().forEach(f -> {
                        originations.add(new Origination(PNodeField.of(node, f), PNodeField.of(b.getNode(), f), ImmutableSet.of(gen)));
                    });

                    node.getBranches().forEach(ob -> {
                        if (ob != b) {
                            checkState(ob.getFields().size() == b.getFields().size());
                            for (int i = 0; i < b.getFields().size(); ++i) {
                                String kf = b.getFields().get(i);
                                String okf = ob.getFields().get(i);
                                originations.add(new Origination(PNodeField.of(node, kf), PNodeField.of(ob.getNode(), okf), ImmutableSet.of(gen)));
                            }
                        }
                    });
                });

                return null;
            }

            @Override
            public Void visitLookup(PLookup node, Void context)
            {
                node.getSource().getFields().getNames().forEach(f -> originations.add(new Origination(
                        PNodeField.of(node, f), PNodeField.of(node.getSource(), f), ImmutableSet.of(OriginGenesis.direct()))));
                node.getBranches().forEach(b -> b.getFields().forEach(f -> originations.add(new Origination(
                        PNodeField.of(node, f), PNodeField.of(b.getNode(), f), ImmutableSet.of(OriginGenesis.join(OriginGenesis.Join.Mode.LOOKUP))))));

                return null;
            }

            @Override
            public Void visitOutput(POutput node, Void context)
            {
                addDirectSingleSource(node);

                return null;
            }

            /*
            originations are BY PNODEFIELDS - NOT SUBVALUES
             - constants irrelevant unless toplevel pure nullary
             - opacity pollutes - any opaque subval makes all funcargs opaque
             - opacity duplicated on each funcarg genesis for easy use in chain walking

            nullary fn:
             pure: CONSTANT
             else: OPAQUE
            non-nullary fn:
             FUNC_ARG w/ variable opacity?
            */
            private List<Origination> buildValueOriginations(
                    PNodeField sink,
                    PNode source,
                    VNode value)
            {
                if (value instanceof VConstant) {
                    return ImmutableList.of(new Origination(
                            sink, OriginGenesis.constant()));
                }

                else if (value instanceof VField) {
                    return ImmutableList.of(new Origination(
                            sink, PNodeField.of(source, ((VField) value).getField()), ImmutableSet.of(OriginGenesis.direct())));
                }

                else if (value instanceof VFunction) {
                    VFunction fn = (VFunction) value;

                    List<Origination> argOriginations = fn.getArgs().stream()
                            .map(a -> buildValueOriginations(sink, source, a))
                            .flatMap(List::stream)
                            .filter(o -> o.source.isPresent())
                            .collect(toImmutableList());

                    argOriginations.forEach(o -> checkState(o.sink == sink));
                    argOriginations.forEach(o -> o.source.ifPresent(osource -> checkState(osource.getNode() == source)));

                    boolean isDeterministic = fn.getFunction().getPurity().isDeterministic();
                    if (argOriginations.isEmpty()) {
                        return ImmutableList.of(new Origination(
                                sink, isDeterministic ? OriginGenesis.external() : OriginGenesis.constant()));
                    }
                    else {
                        return argOriginations.stream()
                                .map(o -> new Origination(sink, o.source, ImmutableSet.of(OriginGenesis.function(isDeterministic))))
                                .collect(toImmutableList());
                    }
                }

                else {
                    throw new IllegalStateException(Objects.toString(value));
                }
            }

            @Override
            public Void visitProject(PProject node, Void context)
            {
                node.getProjection().getInputsByOutput().forEach((o, i) -> {
                    PNodeField sink = PNodeField.of(node, o);
                    List<Origination> valueOriginations = buildValueOriginations(sink, node.getSource(), i);
                    checkNotEmpty(valueOriginations);
                    originations.addAll(valueOriginations);
                });

                return null;
            }

            @Override
            public Void visitScan(PScan node, Void context)
            {
                node.getFields().getNames().forEach(f ->
                        originations.add(new Origination(PNodeField.of(node, f), OriginGenesis.scan())));
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
                        originations.add(new Origination(PNodeField.of(node, f), OriginGenesis.values())));

                return null;
            }
        }, null);

        return new OriginAnalysis(originations, plan.getToposortIndicesByNode());
    }
}
