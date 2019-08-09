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
import com.wrmsr.tokamak.node.visitor.NodeVisitor;
import com.wrmsr.tokamak.type.Type;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Immutable
public final class LookupJoinNode
        extends AbstractNode
        implements InternalNode, JoinNode
{
    @Immutable
    public static final class Branch
    {
        private final Node node;
        private final String field;

        @JsonCreator
        public Branch(
                @JsonProperty("node") Node node,
                @JsonProperty("field") String field)
        {
            this.node = node;
            this.field = field;
        }

        @JsonProperty("node")
        public Node getNode()
        {
            return node;
        }

        @JsonProperty("field")
        public String getField()
        {
            return field;
        }
    }

    private final Node source;
    private final List<Branch> branches;
    private final Optional<String> sourceIdField;

    private final Map<String, Branch> branchesByField;

    @JsonCreator
    public LookupJoinNode(
            @JsonProperty("name") String name,
            @JsonProperty("source") Node source,
            @JsonProperty("branches") List<Branch> branches,
            @JsonProperty("sourceIdField") Optional<String> sourceIdField)
    {
        super(name);

        this.source = source;
        this.branches = ImmutableList.copyOf(branches);
        this.sourceIdField = sourceIdField;

        ImmutableMap.Builder<String, Branch> branchesByField = ImmutableMap.builder();
        for (Branch branch : this.branches) {
            checkArgument(branch.getNode().getFields().containsKey(branch.getField()));
            checkArgument(source.getFields().containsKey(branch.getField()));
            branchesByField.put(branch.getField(), branch);
        }
        this.branchesByField = branchesByField.build();

        checkInvariants();
    }

    @JsonProperty("source'")
    public Node getSource()
    {
        return source;
    }

    @JsonProperty("branches")
    public List<Branch> getBranches()
    {
        return branches;
    }

    @JsonProperty("sourceIdField")
    public Optional<String> getSourceIdField()
    {
        return sourceIdField;
    }

    @Override
    public List<Node> getSources()
    {
        return ImmutableList.<Node>builder()
                .add(source)
                .addAll(branches.stream().map(Branch::getNode).collect(toImmutableList()))
                .build();
    }

    public Map<String, Branch> getBranchesByField()
    {
        return branchesByField;
    }

    @Override
    public Map<String, Type> getFields()
    {
        return source.getFields();
    }

    @Override
    public Set<Set<String>> getIdFieldSets()
    {
        return source.getIdFieldSets();
    }

    @Override
    public <R, C> R accept(NodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitLookupJoinNode(this, context);
    }
}
