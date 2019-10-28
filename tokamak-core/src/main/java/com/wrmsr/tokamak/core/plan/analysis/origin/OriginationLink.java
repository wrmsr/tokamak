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

import javax.annotation.concurrent.Immutable;

import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class OriginationLink
{
    final Origination sink;
    final Set<OriginationLink> next;

    OriginationLink(Origination sink, Set<OriginationLink> next)
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
