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
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.FieldCollection;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitor;
import com.wrmsr.tokamak.util.MoreCollections;
import com.wrmsr.tokamak.util.Pair;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.groupingByImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toCheckSingle;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;

@Immutable
public final class PJoin
        extends PAbstractNode
        implements PJoinLike
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
        private final PNode node;
        private final List<String> fields;

        @JsonCreator
        public Branch(
                @JsonProperty("node") PNode node,
                @JsonProperty("fields") List<String> fields)
        {
            this.node = checkNotNull(node);
            this.fields = ImmutableList.copyOf(fields);
            this.fields.forEach(f -> checkArgument(node.getFields().contains(f)));
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
        public PNode getNode()
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

    private final Map<PNode, Set<Branch>> branchSetsByNode;
    private final Map<Set<String>, Set<Branch>> branchSetsByKeyFieldSet;
    private final Map<Branch, Integer> indicesByBranch;
    private final Map<String, Set<Branch>> branchSetsByField;
    private final Set<String> keyFields;
    private final Map<String, Branch> branchesByUniqueField;
    private final FieldCollection fields;
    private final int keyLength;

    @JsonCreator
    public PJoin(
            @JsonProperty("name") String name,
            @JsonProperty("annotations") PNodeAnnotations annotations,
            @JsonProperty("branches") List<Branch> branches,
            @JsonProperty("mode") Mode mode)
    {
        super(name, annotations);

        this.branches = checkNotEmpty(ImmutableList.copyOf(branches));
        this.mode = checkNotNull(mode);

        branchSetsByNode = this.branches.stream()
                .collect(groupingByImmutableSet(Branch::getNode));
        branchSetsByKeyFieldSet = this.branches.stream()
                .collect(groupingByImmutableSet(b -> ImmutableSet.copyOf(b.getFields())));
        indicesByBranch = IntStream.range(0, this.branches.size()).boxed().collect(toImmutableMap(this.branches::get, identity()));

        branchSetsByField = this.branches.stream()
                .flatMap(b -> b.getNode().getFields().getNames().stream().map(f -> Pair.immutable(f, b)))
                .collect(groupingBy(Pair::first)).entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().stream().map(Pair::second).collect(toImmutableSet())));

        keyFields = this.branches.stream()
                .flatMap(b -> b.getFields().stream())
                .collect(toImmutableSet());
        branchesByUniqueField = this.branchSetsByField.entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .collect(toImmutableMap(Map.Entry::getKey, e -> checkSingle(e.getValue())));

        Map<String, Field> fields = new LinkedHashMap<>();
        for (Branch branch : this.branches) {
            for (Field field : branch.getNode().getFields()) {
                if (!fields.containsKey(field.getName())) {
                    Iterable<FieldAnnotation> fieldAnns = branchesByUniqueField.containsKey(field.getName()) ?
                            field.getAnnotations().onlyTransitive() : ImmutableList.of();
                    fields.put(field.getName(), new Field(field.getName(), field.getType(), fieldAnns));
                }
                else {
                    checkArgument(!keyFields.contains(field.getName()));
                    checkArgument(fields.get(field.getName()).getType().equals(field.getType()));
                }
            }
        }
        this.fields = FieldCollection.of(fields.values())
                .withAnnotations(annotations.getFieldAnnotations());

        keyLength = this.branches.stream().map(b -> b.getFields().size()).distinct().collect(toCheckSingle());

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
    public FieldCollection getFields()
    {
        return fields;
    }

    @Override
    public List<PNode> getSources()
    {
        return branches.stream().map(b -> b.node).collect(toImmutableList());
    }

    public Map<PNode, Set<Branch>> getBranchSetsByNode()
    {
        return branchSetsByNode;
    }

    public Map<Set<String>, Set<Branch>> getBranchSetsByKeyFieldSet()
    {
        return branchSetsByKeyFieldSet;
    }

    public Map<Branch, Integer> getIndicesByBranch()
    {
        return indicesByBranch;
    }

    public Map<String, Set<Branch>> getBranchSetsByField()
    {
        return branchSetsByField;
    }

    public Set<String> getKeyFields()
    {
        return keyFields;
    }

    public Map<String, Branch> getBranchesByUniqueField()
    {
        return branchesByUniqueField;
    }

    public int getKeyLength()
    {
        return keyLength;
    }

    private final SupplierLazyValue<Set<Set<String>>> guaranteedEqualFieldSets = new SupplierLazyValue<>();

    public Set<Set<String>> getGuaranteedEqualFieldSets()
    {
        return guaranteedEqualFieldSets.get(() -> {
            if (mode == Mode.INNER) {
                return ImmutableSet.copyOf(MoreCollections.unify(IntStream.range(0, keyLength)
                        .mapToObj(i -> branches.stream().map(b -> b.getFields().get(i)).collect(toImmutableSet()))
                        .collect(toImmutableSet())));
            }
            else {
                return ImmutableSet.of();
            }
        });
    }

    private final SupplierLazyValue<Map<String, Set<String>>> equivalentFieldSetsByField = new SupplierLazyValue<>();

    public Map<String, Set<String>> getEquivalentFieldSetsByField()
    {
        return equivalentFieldSetsByField.get(() -> {
            Map<String, Set<String>> equivalentFieldsByField = new HashMap<>();
            for (Set<String> fieldSet : getGuaranteedEqualFieldSets()) {
                for (String field : fieldSet) {
                    checkState(!equivalentFieldsByField.containsKey(field));
                    equivalentFieldsByField.put(field, fieldSet);
                }
            }
            for (String field : fields.getNames()) {
                if (!equivalentFieldsByField.containsKey(field)) {
                    equivalentFieldsByField.put(field, ImmutableSet.of(field));
                }
            }
            return ImmutableMap.copyOf(equivalentFieldsByField);
        });
    }

    @Override
    public <R, C> R accept(PNodeVisitor<R, C> visitor, C context)
    {
        return visitor.visitJoin(this, context);
    }
}
