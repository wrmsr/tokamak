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
package com.wrmsr.tokamak.core.plan.node;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;

@Immutable
public final class PNodeField
{
    private final PNode node;
    private final String field;

    public PNodeField(PNode node, String field)
    {
        this.node = checkNotNull(node);
        this.field = checkNotNull(field);
        checkState(node.getFields().contains(field));
    }

    @Override
    public String toString()
    {
        return "PNodeField{" +
                "node=" + node +
                ", field='" + field + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        PNodeField nodeField = (PNodeField) o;
        return Objects.equals(node, nodeField.node) &&
                Objects.equals(field, nodeField.field);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(node, field);
    }

    public PNode getNode()
    {
        return node;
    }

    public String getField()
    {
        return field;
    }

    public static PNodeField of(PNode node, String field)
    {
        return new PNodeField(node, field);
    }

    public static <V> Map<PNode, Map<String, V>> expandNodeFieldMap(Map<PNodeField, V> in)
    {
        Map<PNode, ImmutableMap.Builder<String, V>> out = new LinkedHashMap<>();
        in.forEach((k, v) -> out.computeIfAbsent(k.node, n -> ImmutableMap.builder()).put(k.field, v));
        return out.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
    }

    public static <V> Map<PNodeField, V> flattenNodeFieldMap(Map<PNode, Map<String, V>> in)
    {
        ImmutableMap.Builder<PNodeField, V> out = ImmutableMap.builder();
        in.forEach((k0, m) -> m.forEach((k1, v) -> out.put(of(k0, k1), v)));
        return out.build();
    }
}
