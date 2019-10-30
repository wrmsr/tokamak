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
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;
import static com.wrmsr.tokamak.util.MoreCollections.sorted;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;
import static com.wrmsr.tokamak.util.func.RecursiveConsumer.acceptRecursive;

@Immutable
public final class OriginChainAnalysis
{
    private final OriginAnalysis originAnalysis;
    private final Predicate<Origination> splitPredicate;

    private final Set<Origination> firstOriginations;
    private final Map<PNodeField, Set<Origination>> firstOriginationSetsBySink;
    private final Map<Origination, Set<Origination>> firstOriginationSetsByOrigination;
    private final Map<PNodeField, Set<OriginationLink>> originationLinkSetsBySink;

    OriginChainAnalysis(OriginAnalysis originAnalysis, Predicate<Origination> splitPredicate)
    {
        this.originAnalysis = checkNotNull(originAnalysis);
        this.splitPredicate = checkNotNull(splitPredicate);

        Set<Origination> firstOriginations = new LinkedHashSet<>();
        Map<PNodeField, Set<Origination>> firstOriginationSetsBySink = new LinkedHashMap<>();
        Map<Origination, Set<Origination>> firstOriginationSetsByOrigination = new LinkedHashMap<>();
        Map<PNodeField, Set<OriginationLink>> originationLinkSetsBySink = new LinkedHashMap<>();

        sorted(
                originAnalysis.originationSetsBySinkNodeBySinkField.keySet(),
                Comparator.comparing(originAnalysis.toposortIndicesByNode::get)
        ).forEach(snkNode -> {
            originAnalysis.originationSetsBySinkNodeBySinkField.get(snkNode).forEach((snkField, snkOris) -> {
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
                    checkState(!v.source.isPresent() || splitPredicate.test(v));
                    ret.computeIfAbsent(v.sink, v_ -> new LinkedHashSet<>()).add(k);
                });
            });
            return newImmutableSetMap(ret);
        });
    }

    public void forEachPath(PNodeField sink, PNodeField source, Consumer<List<OriginationLink>> consumer)
    {
        int sourceToposortIndex = originAnalysis.toposortIndicesByNode.get(source.getNode());
        checkState(originAnalysis.toposortIndicesByNode.get(sink.getNode()) >= sourceToposortIndex);
        originationLinkSetsBySink.get(sink).forEach(start -> {
            List<OriginationLink> path = new ArrayList<>();
            acceptRecursive((rec, link) -> {
                path.add(link);
                if (link.sink.sink.equals(source)) {
                    consumer.accept(ImmutableList.copyOf(path));
                }
                else if (originAnalysis.toposortIndicesByNode.get(link.sink.sink.getNode()) > sourceToposortIndex) {
                    link.next.forEach(rec);
                }
                checkState(path.remove(path.size() - 1) == link);
            }, start);
        });
    }

    public List<List<OriginationLink>> getPaths(PNodeField sink, PNodeField source)
    {
        ImmutableList.Builder<List<OriginationLink>> builder = ImmutableList.builder();
        forEachPath(sink, source, builder::add);
        return builder.build();
    }
}
