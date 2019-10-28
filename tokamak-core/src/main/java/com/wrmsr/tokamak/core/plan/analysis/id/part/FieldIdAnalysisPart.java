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
import com.google.common.collect.Iterators;

import javax.annotation.concurrent.Immutable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class FieldIdAnalysisPart
        extends IdAnalysisPart
{
    private final String field;

    FieldIdAnalysisPart(String field)
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
        FieldIdAnalysisPart fieldPart = (FieldIdAnalysisPart) o;
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
    public Set<String> getFields()
    {
        return ImmutableSet.of(field);
    }

    @Override
    public Iterator<String> iterator()
    {
        return Iterators.singletonIterator(field);
    }
}
