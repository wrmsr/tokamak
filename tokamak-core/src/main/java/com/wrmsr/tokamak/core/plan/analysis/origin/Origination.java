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

import com.wrmsr.tokamak.core.plan.node.PNodeField;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Immutable
public final class Origination
{
    final PNodeField sink;
    final Optional<PNodeField> source;
    final Genesis genesis;
    final OriginNesting nesting;

    Origination(PNodeField sink, Optional<PNodeField> source, Genesis genesis, OriginNesting nesting)
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
            checkArgument(nesting instanceof OriginNesting.None);
        }
    }

    Origination(PNodeField sink, PNodeField source, Genesis genesis, OriginNesting nesting)
    {
        this(sink, Optional.of(source), genesis, nesting);
    }

    Origination(PNodeField sink, Genesis genesis)
    {
        this(sink, Optional.empty(), genesis, OriginNesting.none());
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
