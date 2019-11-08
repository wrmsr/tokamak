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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class PLookup
        extends PAbstractNode
        implements PInternal, PJoinLike
{
    @Immutable
    public static final class Branch
    {
        private final PNode node;
        private final Set<String> fields;

        @JsonCreator
        public Branch(
                @JsonProperty("node") PNode node,
                @JsonProperty("fields") Set<String> fields)
        {
            this.node = checkNotNull(node);
            this.fields = ImmutableSet.copyOf(checkOrdered(fields));
            this.fields.forEach(f -> checkArgument(node.getFields().contains(f)));
        }

        @JsonProperty("node")
        public PNode getNode()
        {
            return node;
        }

        @JsonProperty("fields")
        public Set<String> getFields()
        {
            return fields;
        }
    }

    private final PNode source;
    private final Set<String> sourceKeyFields;
    private final List<Branch> branches;

    private final Map<Set<String>, Branch> branchesByFieldSets;

    @JsonCreator
    public PLookup(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("source") PNode source,
            @JsonProperty("sourceKeyFields") Set<String> sourceKeyFields,
            @JsonProperty("branches") List<Branch> branches)
    {
        super(name, annotations);

        this.source = checkNotNull(source);
        this.sourceKeyFields = ImmutableSet.copyOf(checkOrdered(sourceKeyFields));
        this.branches = checkNotEmpty(ImmutableList.copyOf(branches));

        this.sourceKeyFields.forEach(f -> checkArgument(source.getFields().contains(f)));

        ImmutableMap.Builder<Set<String>, Branch> branchesByFieldSets = ImmutableMap.builder();
        for (Branch branch : this.branches) {
            branch.getFields().forEach(f -> checkArgument(source.getFields().contains(f)));
            branchesByFieldSets.put(branch.getFields(), branch);
        }
        this.branchesByFieldSets = branchesByFieldSets.build();

        checkInvariants();
    }

    @JsonProperty("source'")
    public PNode getSource()
    {
        return source;
    }

    @JsonProperty("sourceKeyFields")
    public Set<String> getSourceKeyFields()
    {
        return sourceKeyFields;
    }

    @JsonProperty("branches")
    public List<Branch> getBranches()
    {
        return branches;
    }

    @Override
    public List<PNode> getSources()
    {
        return ImmutableList.<PNode>builder()
                .add(source)
                .addAll(branches.stream().map(Branch::getNode).collect(toImmutableList()))
                .build();
    }

    @Override
    public FieldCollection getFields()
    {
        return source.getFields();
    }

    public Map<Set<String>, Branch> getBranchesByFieldSets()
    {
        return branchesByFieldSets;
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitLookup(this, context);
    }
}
