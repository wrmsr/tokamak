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
package com.wrmsr.tokamak.core.plan.analysis.id.part;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.MoreCollections;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

@Immutable
public abstract class IdAnalysisPart
        implements StreamableIterable<String>
{
    public abstract Set<String> getFields();

    public boolean contains(String field)
    {
        checkNotNull(field);
        return stream().anyMatch(field::equals);
    }

    public static IdAnalysisPart of(Iterable<String> fields)
    {
        Set<String> set = ImmutableSet.copyOf(fields);
        if (set.size() == 1) {
            return new FieldIdAnalysisPart(checkSingle(set));
        }
        else if (set.size() > 1) {
            return new SetIdAnalysisPart(ImmutableSet.copyOf(set));
        }
        else {
            throw new IllegalArgumentException(Objects.toString(set));
        }
    }

    public static IdAnalysisPart of(String... fields)
    {
        return of(ImmutableList.copyOf(fields));
    }

    public static List<IdAnalysisPart> unify(Iterable<IdAnalysisPart> parts)
    {
        List<Set<String>> unified = MoreCollections.unify(StreamSupport.stream(parts.spliterator(), false)
                .map(ImmutableSet::copyOf)
                .collect(toImmutableList()));
        return unified.stream().map(IdAnalysisPart::of).collect(toImmutableList());
    }
}
