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

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.util.collect.StreamableIterable;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class SetIdAnalysisPart
        extends IdAnalysisPart
        implements StreamableIterable<String>
{
    private final Set<String> fields;

    SetIdAnalysisPart(Set<String> fields)
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
        SetIdAnalysisPart strings = (SetIdAnalysisPart) o;
        return Objects.equals(fields, strings.fields);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fields);
    }

    @Override
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
