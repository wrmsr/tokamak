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
package com.wrmsr.tokamak.core.plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.PNodeId;
import com.wrmsr.tokamak.util.NameGenerator;
import com.wrmsr.tokamak.util.collect.Toposort;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollections.buildListIndexMap;
import static com.wrmsr.tokamak.util.MoreCollections.reversedImmutableListOf;
import static com.wrmsr.tokamak.util.MoreCollections.sorted;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
@Immutable
public final class Plan
{
    private final PNode root;

    private final Set<PNode> nodes;
    private final Map<String, PNode> nodesByName;
    private final Map<PNodeId, PNode> nodesByNodeId;

    @JsonCreator
    private Plan(
            @JsonProperty("root") PNode root)
    {
        this.root = root;

        Set<PNode> nodes = new HashSet<>();
        Map<String, PNode> nodesByName = new HashMap<>();
        Map<PNodeId, PNode> nodesById = new HashMap<>();

        List<PNode> nodeStack = new ArrayList<>();
        nodeStack.add(root);
        while (!nodeStack.isEmpty()) {
            PNode cur = nodeStack.remove(nodeStack.size() - 1);
            checkState(!nodes.contains(cur));
            checkArgument(!nodesByName.containsKey(cur.getName()));
            checkArgument(!nodesById.containsKey(cur.getId()));
            nodes.add(cur);
            nodesByName.put(cur.getName(), cur);
            nodesById.put(cur.getId(), cur);
            cur.getSources().stream()
                    .filter(n -> !nodes.contains(n))
                    .forEach(nodeStack::add);
        }

        this.nodes = ImmutableSet.copyOf(nodes);
        this.nodesByName = ImmutableMap.copyOf(nodesByName);
        this.nodesByNodeId = ImmutableMap.copyOf(nodesById);

        PlanValidation.validatePlan(this);
    }

    public static Plan of(PNode root)
    {
        return new Plan(root);
    }

    @JsonProperty("root")
    public PNode getRoot()
    {
        return root;
    }

    public Set<PNode> getNodes()
    {
        return nodes;
    }

    public Map<String, PNode> getNodesByName()
    {
        return nodesByName;
    }

    public Set<String> getNodeNames()
    {
        return nodesByName.keySet();
    }

    public Map<PNodeId, PNode> getNodesById()
    {
        return nodesByNodeId;
    }

    public PNode getNode(String name)
    {
        return checkNotNull(nodesByName.get(name));
    }

    public PNode getNode(PNodeId id)
    {
        return checkNotNull(nodesByNodeId.get(id));
    }

    private final SupplierLazyValue<List<PNode>> nameSortedNodes = new SupplierLazyValue<>();

    public List<PNode> getNameSortedNodes()
    {
        return nameSortedNodes.get(() -> nodes.stream()
                .sorted(Comparator.comparing(PNode::getName))
                .collect(toImmutableList()));
    }

    private final SupplierLazyValue<Map<PNode, Integer>> nameSortedIndicesByNode = new SupplierLazyValue<>();

    public Map<PNode, Integer> getNameSortedIndicesByNode()
    {
        return nameSortedIndicesByNode.get(() -> buildListIndexMap(getNameSortedNodes()));
    }

    @SuppressWarnings({"rawtypes"})
    private final Map<Class<? extends PNode>, List> nodeListsByType = new HashMap<>();

    @SuppressWarnings({"unchecked"})
    public <T extends PNode> List<T> getNodeTypeList(Class<T> nodeType)
    {
        return nodeListsByType.computeIfAbsent(nodeType, nt -> {
            ImmutableList.Builder<T> ret = ImmutableList.builder();
            for (PNode node : getNameSortedNodes()) {
                if (nodeType.isInstance(node)) {
                    ret.add((T) node);
                }
            }
            return ret.build();
        });
    }

    private final SupplierLazyValue<Set<PNode>> leafNodes = new SupplierLazyValue<>();

    public Set<PNode> getLeafNodes()
    {
        return leafNodes.get(() -> getNameSortedNodes().stream()
                .filter(n -> n.getSources().isEmpty())
                .collect(toImmutableSet()));
    }

    private final SupplierLazyValue<List<Set<PNode>>> nodeToposort = new SupplierLazyValue<>();

    public List<Set<PNode>> getNodeToposort()
    {
        return nodeToposort.get(() -> {
            List<Set<PNode>> ts = Toposort.toposort(getNameSortedNodes().stream()
                    .collect(toImmutableMap(identity(), n -> ImmutableSet.copyOf(n.getSources()))));
            return ts.stream()
                    .map(step -> ImmutableSet.copyOf(sorted(step, Comparator.comparing(getNameSortedIndicesByNode()::get))))
                    .collect(toImmutableList());
        });
    }

    private final SupplierLazyValue<List<Set<PNode>>> nodeReverseToposort = new SupplierLazyValue<>();

    public List<Set<PNode>> getNodeReverseToposort()
    {
        return nodeReverseToposort.get(() -> reversedImmutableListOf(getNodeToposort().stream()
                .map(step -> ImmutableSet.copyOf(reversedImmutableListOf(step))).collect(toImmutableList())));
    }

    private final SupplierLazyValue<List<PNode>> toposortedNodes = new SupplierLazyValue<>();

    public List<PNode> getToposortedNodes()
    {
        return toposortedNodes.get(() -> getNodeToposort().stream()
                .flatMap(Set::stream)
                .collect(toImmutableList()));
    }

    private final SupplierLazyValue<List<PNode>> reverseToposortedNodes = new SupplierLazyValue<>();

    public List<PNode> getReverseToposortedNodes()
    {
        return reverseToposortedNodes.get(() -> getNodeReverseToposort().stream()
                .flatMap(Set::stream)
                .collect(toImmutableList()));
    }

    private final SupplierLazyValue<Map<PNode, Integer>> toposortIndicesByNode = new SupplierLazyValue<>();

    public Map<PNode, Integer> getToposortIndicesByNode()
    {
        return toposortIndicesByNode.get(() -> buildListIndexMap(getToposortedNodes()));
    }

    private final SupplierLazyValue<Map<PNode, Integer>> reverseToposortIndicesByNode = new SupplierLazyValue<>();

    public Map<PNode, Integer> getReverseToposortIndicesByNode()
    {
        return reverseToposortIndicesByNode.get(() -> buildListIndexMap(getReverseToposortedNodes()));
    }

    private final SupplierLazyValue<List<PNodeField>> toposortedNodeFields = new SupplierLazyValue<>();

    public List<PNodeField> getToposortedNodeFields()
    {
        return toposortedNodeFields.get(() -> getToposortedNodes().stream()
                .flatMap(n -> n.getFields().getNameList().stream().map(f -> PNodeField.of(n, f)))
                .collect(toImmutableList()));
    }

    private final SupplierLazyValue<Map<PNodeField, Integer>> toposortIndicesByNodeField = new SupplierLazyValue<>();

    public Map<PNodeField, Integer> getToposortIndicesByNodeField()
    {
        return toposortIndicesByNodeField.get(() -> buildListIndexMap(getToposortedNodeFields()));
    }

    public static final String NAME_GENERATOR_PREFIX = "@";

    private final SupplierLazyValue<NameGenerator> nodeNameGenerator = new SupplierLazyValue<>();

    public NameGenerator getNodeNameGenerator()
    {
        return nodeNameGenerator.get(() -> new NameGenerator(nodesByName.keySet(), NAME_GENERATOR_PREFIX));
    }

    private final SupplierLazyValue<NameGenerator> fieldNameGenerator = new SupplierLazyValue<>();

    public NameGenerator getFieldNameGenerator()
    {
        return fieldNameGenerator.get(() ->
                new NameGenerator(nodes.stream().flatMap(n -> n.getFields().getNames().stream()).collect(toImmutableSet()), NAME_GENERATOR_PREFIX));
    }
}
