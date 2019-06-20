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
package com.wrmsr.tokamak.materialization.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.materialization.api.FieldName;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.node.visitor.NodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
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
        private final FieldName field;

        public Branch(Node node, FieldName field)
        {
            this.node = node;
            this.field = field;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Branch branch = (Branch) o;
            return Objects.equals(node, branch.node) &&
                    Objects.equals(field, branch.field);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(node, field);
        }

        public Node getNode()
        {
            return node;
        }

        public FieldName getField()
        {
            return field;
        }
    }

    private final List<Branch> branches;
    private final Mode mode;

    private final Map<Node, Branch> branchesByNode;
    private final Map<FieldName, Set<Node>> nodeSetsByField;
    private final Map<FieldName, Node> nodesByUniqueField;
    private final Set<FieldName> guaranteedUniqueFields;
    private final Set<FieldName> fields;
    private final Set<FieldName> idFields;

    public EquijoinNode(
            NodeName name,
            Iterable<Branch> branches,
            Mode mode)
    {
        super(name);

        this.branches = ImmutableList.copyOf(branches);
        this.mode = mode;

        this.branchesByNode = this.branches.stream().collect(toImmutableMap(Branch::getNode, identity()));

        Map<FieldName, ImmutableSet.Builder<Node>> nodeSetsByField = new HashMap<>();
        for (Branch branch : this.branches) {
            for (FieldName field : branch.node.getFields()) {
                nodeSetsByField.computeIfAbsent(field, n -> ImmutableSet.builder()).add(branch.node);
            }
        }
        this.nodeSetsByField = nodeSetsByField.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
        nodesByUniqueField = this.nodeSetsByField.entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().findFirst().get()));
        guaranteedUniqueFields = this.nodeSetsByField.entrySet().stream()
                .filter(e -> e.getValue().stream().allMatch(n -> this.branchesByNode.get(n).field == e.getKey()))
                .map(Map.Entry::getKey)
                .collect(toImmutableSet());

        Set<FieldName> fields = new LinkedHashSet<>();
        for (Branch branch : this.branches) {
            checkArgument(branch.node.getFields().contains(branch.getField()));
            for (FieldName field : branch.getNode().getFields()) {
                if (!fields.add(field)) {
                    checkArgument(guaranteedUniqueFields.contains(field));
                }
            }
        }
        this.fields = ImmutableSet.copyOf(fields);

        idFields = this.branches.stream().map(Branch::getField).collect(toImmutableSet());

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

    public Map<FieldName, Set<Node>> getNodeSetsByField()
    {
        return nodeSetsByField;
    }

    public Map<FieldName, Node> getNodesByUniqueField()
    {
        return nodesByUniqueField;
    }

    public Set<FieldName> getGuaranteedUniqueFields()
    {
        return guaranteedUniqueFields;
    }

    @Override
    public Set<FieldName> getFields()
    {
        return fields;
    }

    @Override
    public Set<FieldName> getIdFields()
    {
        return idFields;
    }

    @Override
    public Set<Node> getChildren()
    {
        return branches.stream().map(b -> b.node).collect(toImmutableSet());
    }

    @Override
    public <C, R> R accept(NodeVisitor<C, R> visitor, C context)
    {
        return visitor.visitEquijoinNode(this, context);
    }
}
