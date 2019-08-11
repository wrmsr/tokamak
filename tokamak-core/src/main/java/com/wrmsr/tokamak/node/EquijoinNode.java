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
import com.google.common.collect.Sets;
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;
import com.wrmsr.tokamak.util.Pair;

import javax.annotation.concurrent.Immutable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.groupingBySet;
import static com.wrmsr.tokamak.util.MoreCollectors.toSingle;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.stream.Collectors.groupingBy;

@Immutable
public final class EquijoinNode
        extends AbstractNode
        implements JoinNode
{
    public enum Mode
    {
        INNER,
        LEFT,
        FULL
    }

    @Immutable
    public static final class Branch
    {
        private final Node node;
        private final List<String> fields;

        @JsonCreator
        public Branch(
                @JsonProperty("node") Node node,
                @JsonProperty("fields") List<String> fields)
        {
            this.node = node;
            this.fields = checkNotEmpty(ImmutableList.copyOf(fields));
            this.fields.forEach(f -> checkArgument(node.getFields().containsKey(f)));
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Branch branch = (Branch) o;
            return Objects.equals(node, branch.node) &&
                    Objects.equals(fields, branch.fields);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(node, fields);
        }

        @Override
        public String toString()
        {
            return "Branch{" +
                    "node=" + node +
                    ", fields=" + fields +
                    '}';
        }

        @JsonProperty("node")
        public Node getNode()
        {
            return node;
        }

        @JsonProperty("fields")
        public List<String> getFields()
        {
            return fields;
        }
    }

    private final List<Branch> branches;
    private final Mode mode;

    private final Map<Node, Set<Branch>> branchSetsByNode;
    private final Map<Set<String>, Set<Branch>> branchSetsByIdFieldSets;
    private final Map<String, Set<Branch>> branchSetsByField;
    private final Set<String> idFields;
    private final Map<String, Branch> branchesByUniqueField;
    private final Map<String, Type> fields;
    private final Set<Set<String>> idFieldSets;

    @JsonCreator
    public EquijoinNode(
            @JsonProperty("name") String name,
            @JsonProperty("branches") List<Branch> branches,
            @JsonProperty("mode") Mode mode)
    {
        super(name);

        this.branches = checkNotEmpty(ImmutableList.copyOf(branches));
        this.mode = checkNotNull(mode);

        branchSetsByNode = this.branches.stream()
                .collect(groupingBySet(Branch::getNode));
        branchSetsByIdFieldSets = this.branches.stream()
                .collect(groupingBySet(b -> ImmutableSet.copyOf(b.getFields())));

        branchSetsByField = this.branches.stream()
                .flatMap(b -> b.getNode().getFields().keySet().stream().map(f -> Pair.immutable(f, b)))
                .collect(groupingBy(Pair::first)).entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().map(Pair::second).collect(toImmutableSet())));

        idFields = this.branches.stream()
                .flatMap(b -> b.getFields().stream())
                .collect(toImmutableSet());
        branchesByUniqueField = this.branchSetsByField.entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().findFirst().get()));

        Set<String> duplicateNonKeyFields = Sets.intersection(idFields, branchesByUniqueField.keySet());
        if (!duplicateNonKeyFields.isEmpty()) {
            throw new IllegalStateException(duplicateNonKeyFields.toString());
        }

        Map<String, Type> fields = new LinkedHashMap<>();
        for (Branch branch : this.branches) {
            for (Map.Entry<String, Type> entry : branch.getNode().getFields().entrySet()) {
                if (!fields.containsKey(entry.getKey())) {
                    fields.put(entry.getKey(), entry.getValue());
                }
                else {
                    checkArgument(fields.get(entry.getKey()).equals(entry.getValue()));
                }
            }
        }
        this.fields = ImmutableMap.copyOf(fields);

        int keyLength = this.branches.stream().map(b -> b.getFields().size()).distinct().collect(toSingle());
        idFieldSets = IntStream.range(0, keyLength)
                .mapToObj(i -> this.branches.stream().map(b -> b.getFields().get(i)).collect(toImmutableSet()))
                .collect(toImmutableSet());

        checkInvariants();
    }

    @JsonProperty("branches")
    public List<Branch> getBranches()
    {
        return branches;
    }

    @JsonProperty("mode")
    public Mode getMode()
    {
        return mode;
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
    public List<Node> getSources()
    {
        return branches.stream().map(b -> b.node).collect(toImmutableList());
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitEquijoinNode(this, context);
    }
}
