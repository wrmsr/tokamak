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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.Function.identity;

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
            this.fields = ImmutableList.copyOf(fields);
            checkArgument(!this.fields.isEmpty());
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

    private final Map<Node, Branch> branchesByNode;
    private final Map<String, Set<Node>> nodeSetsByField;
    private final Map<String, Node> nodesByUniqueField;
    private final Set<Set<String>> idFieldSets;
    private final Set<Set<String>> guaranteedEqualFieldSets;
    private final Map<String, Type> fields;

    public EquijoinNode(
            String name,
            List<Branch> branches,
            Mode mode)
    {
        super(name);

        this.branches = ImmutableList.copyOf(branches);
        this.mode = mode;

        this.branchesByNode = this.branches.stream().collect(toImmutableMap(Branch::getNode, identity()));

        Map<String, ImmutableSet.Builder<Node>> nodeSetsByField = new HashMap<>();
        for (Branch branch : this.branches) {
            for (String field : branch.node.getFields().keySet()) {
                nodeSetsByField.computeIfAbsent(field, n -> ImmutableSet.builder()).add(branch.node);
            }
        }
        this.nodeSetsByField = nodeSetsByField.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
        nodesByUniqueField = this.nodeSetsByField.entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().findFirst().get()));

        idFieldSets = this.branches.stream().map(b -> ImmutableSet.copyOf(b.getFields())).collect(toImmutableSet());

        guaranteedEqualFields = this.nodeSetsByField.entrySet().stream()
                .filter(e -> e.getValue().stream().allMatch(n -> this.branchesByNode.get(n).field.equals(e.getKey())))
                .map(Map.Entry::getKey)
                .collect(toImmutableSet());

        Map<String, Type> fields = new LinkedHashMap<>();
        for (Branch branch : this.branches) {
            checkArgument(branch.node.getFields().containsKey(branch.getField()));
            for (Map.Entry<String, Type> entry : branch.getNode().getFields().entrySet()) {
                if (!fields.containsKey(entry.getKey())) {
                    fields.put(entry.getKey(), entry.getValue());
                }
                else {
                    checkArgument(guaranteedEqualFieldSets.contains(entry.getKey()));
                    checkArgument(fields.get(entry.getKey()).equals(entry.getValue()));
                }
            }
        }
        this.fields = ImmutableMap.copyOf(fields);

        checkInvariants();
    }

    public List<Branch> getBranches()
    {
        return branches;
    }

    public Mode getMode()
    {
        return mode;
    }

    public Map<Node, Branch> getBranchesByNode()
    {
        return branchesByNode;
    }

    public Map<String, Set<Node>> getNodeSetsByField()
    {
        return nodeSetsByField;
    }

    public Map<String, Node> getNodesByUniqueField()
    {
        return nodesByUniqueField;
    }

    public Set<Set<String>> getGuaranteedEqualFieldSets()
    {
        return guaranteedEqualFieldSets;
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
