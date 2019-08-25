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
package com.wrmsr.tokamak.driver.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.api.Id;
import com.wrmsr.tokamak.api.NodeId;

import javax.annotation.concurrent.Immutable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Immutable
public final class Linkage
{
    public interface Links
    {
    }

    @Immutable
    public static final class IdLinks
            implements Links
    {
        private final Set<Id> ids;

        public IdLinks(Set<Id> ids)
        {
            this.ids = ImmutableSet.copyOf(ids);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            IdLinks idLinks = (IdLinks) o;
            return Objects.equals(ids, idLinks.ids);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(ids);
        }

        public Set<Id> getIds()
        {
            return ids;
        }
    }

    @Immutable
    public static final class DenormalizedLinks
            implements Links
    {
        private final Map<Id, Object[]> attributesById;

        public DenormalizedLinks(Map<Id, Object[]> attributesById)
        {
            this.attributesById = ImmutableMap.copyOf(attributesById);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            DenormalizedLinks that = (DenormalizedLinks) o;
            return Objects.equals(attributesById, that.attributesById);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(attributesById);
        }

        public Map<Id, Object[]> getAttributesById()
        {
            return attributesById;
        }
    }

    private final Map<NodeId, Links> input;
    private final Map<NodeId, Links> output;

    public static final Linkage EMPTY = new Linkage(
            ImmutableMap.of(),
            ImmutableMap.of());

    public Linkage(Map<NodeId, Links> input, Map<NodeId, Links> output)
    {
        this.input = ImmutableMap.copyOf(input);
        this.output = ImmutableMap.copyOf(output);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Linkage linkage = (Linkage) o;
        return Objects.equals(input, linkage.input) &&
                Objects.equals(output, linkage.output);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(input, output);
    }

    public Map<NodeId, Links> getInput()
    {
        return input;
    }

    public Map<NodeId, Links> getOutput()
    {
        return output;
    }
}
