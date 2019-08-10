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
package com.wrmsr.tokamak.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.Function.identity;

@Immutable
public final class UnionNode
        extends AbstractNode
{
    private final List<Node> sources;
    private final Optional<String> indexField;

    private final Map<String, Type> fields;
    private final Set<Set<String>> idFieldSets;

    @JsonCreator
    public UnionNode(
            @JsonProperty("name") String name,
            @JsonProperty("sources") List<Node> sources,
            @JsonProperty("indexField") Optional<String> indexField)
    {
        super(name);

        this.sources = checkNotEmpty(ImmutableList.copyOf(sources));
        this.indexField = checkNotNull(indexField);

        Map<String, Type> firstFields = this.sources.get(0).getFields();
        for (int i = 1; i < this.sources.size(); ++i) {
            checkArgument(firstFields.equals(this.sources.get(i).getFields()));
        }

        ImmutableMap.Builder<String, Type> fields = ImmutableMap.builder();
        fields.putAll(firstFields);
        indexField.ifPresent(f -> fields.put(f, Type.LONG));
        this.fields = fields.build();

        if (indexField.isPresent()) {
            Map<Set<String>, Long> idFieldSetCounts = this.sources.stream()
                    .map(Node::getIdFieldSets)
                    .flatMap(Set::stream)
                    .collect(Collectors.groupingBy(identity(), Collectors.counting()));
            ImmutableSet.Builder<Set<String>> idFieldSets = ImmutableSet.builder();
            for (Map.Entry<Set<String>, Long> e : idFieldSetCounts.entrySet()) {
                checkState(e.getValue() <= this.sources.size());
                if (e.getValue() == this.sources.size()) {
                    idFieldSets.add(ImmutableSet.<String>builder().addAll(e.getKey()).add(indexField.get()).build());
                }
            }
            this.idFieldSets = idFieldSets.build();
        }
        else {
            this.idFieldSets = ImmutableSet.of();
        }

        checkInvariants();
    }

    @JsonProperty("sources")
    @Override
    public List<Node> getSources()
    {
        return sources;
    }

    @JsonProperty("indexField")
    public Optional<String> getIndexField()
    {
        return indexField;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return fields;
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        return idFieldSets;
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitUnionNode(this, context);
    }
}
