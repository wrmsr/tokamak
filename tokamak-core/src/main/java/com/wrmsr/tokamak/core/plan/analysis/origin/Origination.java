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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.util.Pair;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

@Immutable
public final class Origination
{
    final PNodeField sink;
    final Optional<PNodeField> source;
    final Set<OriginGenesis> geneses;

    Origination(PNodeField sink, Optional<PNodeField> source, Iterable<OriginGenesis> geneses)
    {
        this.sink = checkNotNull(sink);
        this.source = checkNotNull(source);
        this.geneses = checkNotEmpty(ImmutableSet.copyOf(geneses));
        if (source.isPresent()) {
            PNodeField src = source.get();
            checkState(src != sink);
            checkState(sink.getNode().getSources().contains(src.getNode()));
            this.geneses.forEach(g -> checkArgument(!g.isLeaf()));
        }
        else {
            this.geneses.forEach(g -> checkArgument(g.isLeaf()));
        }
    }

    Origination(PNodeField sink, PNodeField source, Iterable<OriginGenesis> geneses)
    {
        this(sink, Optional.of(source), geneses);
    }

    Origination(PNodeField sink, OriginGenesis genesis)
    {
        this(sink, Optional.empty(), ImmutableSet.of(genesis));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Origination that = (Origination) o;
        return Objects.equals(sink, that.sink) &&
                Objects.equals(source, that.source) &&
                Objects.equals(geneses, that.geneses);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sink, source, geneses);
    }

    @Override
    public String toString()
    {
        return "Origination{" +
                "sink=" + sink +
                ", source=" + source +
                ", geneses=" + geneses +
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

    public Set<OriginGenesis> getGeneses()
    {
        return geneses;
    }

    public OriginGenesis getGenesis()
    {
        return checkSingle(geneses);
    }

    public static List<Origination> merge(Iterable<Origination> originations)
    {
        return merge(originations.iterator());
    }

    public static List<Origination> merge(Iterator<Origination> originations)
    {
        Map<Pair<PNodeField, Optional<PNodeField>>, Set<OriginGenesis>> genesisSetsByPair = new LinkedHashMap<>();
        while (originations.hasNext()) {
            Origination origination = checkNotNull(originations.next());
            genesisSetsByPair.computeIfAbsent(Pair.immutable(origination.sink, origination.source), p -> new LinkedHashSet<>())
                    .addAll(origination.geneses);
        }
        return genesisSetsByPair.entrySet().stream()
                .map(p -> new Origination(p.getKey().getFirst(), p.getKey().getSecond(), p.getValue()))
                .collect(toImmutableList());
    }
}
