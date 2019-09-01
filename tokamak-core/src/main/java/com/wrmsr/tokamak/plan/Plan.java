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
package com.wrmsr.tokamak.plan;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.wrmsr.tokamak.api.NodeId;
import com.wrmsr.tokamak.node.Node;
import com.wrmsr.tokamak.util.Pair;
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
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

@Immutable
public final class Plan
{
    private final Node root;

    private final Set<Node> nodes;
    private final Map<String, Node> nodesByName;
    private final Map<NodeId, Node> nodesByNodeId;

    public Plan(Node root)
    {
        this.root = root;

        Set<Node> nodes = new HashSet<>();
        Map<String, Node> nodesByName = new HashMap<>();
        Map<NodeId, Node> nodesById = new HashMap<>();

        List<Node> nodeStack = new ArrayList<>();
        nodeStack.add(root);
        while (!nodeStack.isEmpty()) {
            Node cur = nodeStack.remove(nodeStack.size() - 1);
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
    }

    public Node getRoot()
    {
        return root;
    }

    public Set<Node> getNodes()
    {
        return nodes;
    }

    public Map<String, Node> getNodesByName()
    {
        return nodesByName;
    }

    public Map<NodeId, Node> getNodesById()
    {
        return nodesByNodeId;
    }

    private final SupplierLazyValue<List<Node>> nameSortedNodes = new SupplierLazyValue<>();

    public List<Node> getNameSortedNodes()
    {
        return nameSortedNodes.get(() -> nodes.stream()
                .sorted(Comparator.comparing(Node::getName))
                .collect(toImmutableList()));
    }

    private final Map<Class<? extends Node>, List> nodeListsByType = new HashMap<>();

    @SuppressWarnings({"unchecked"})
    public <T extends Node> List<T> getNodeTypeList(Class<T> nodeType)
    {
        return nodeListsByType.computeIfAbsent(nodeType, nt -> {
            ImmutableList.Builder<T> ret = ImmutableList.builder();
            for (Node node : getNameSortedNodes()) {
                if (nodeType.isInstance(node)) {
                    ret.add((T) node);
                }
            }
            return ret.build();
        });
    }

    private final SupplierLazyValue<Set<Node>> leafNodes = new SupplierLazyValue<>();

    public Set<Node> getLeafNodes()
    {
        return leafNodes.get(() -> getNameSortedNodes().stream()
                .filter(n -> n.getSources().isEmpty())
                .collect(toImmutableSet()));
    }

    private final SupplierLazyValue<List<Set<Node>>> nodeToposort = new SupplierLazyValue<>();

    public List<Set<Node>> getNodeToposort()
    {
        return nodeToposort.get(() -> Toposort.toposort(getNameSortedNodes().stream()
                .collect(toImmutableMap(identity(), n -> ImmutableSet.copyOf(n.getSources())))));
    }

    private final SupplierLazyValue<List<Set<Node>>> nodeReverseToposort = new SupplierLazyValue<>();

    public List<Set<Node>> getNodeReverseToposort()
    {
        return nodeReverseToposort.get(() -> ImmutableList.copyOf(Lists.reverse(getNodeToposort())));
    }

    private final SupplierLazyValue<List<Node>> toposortedNodes = new SupplierLazyValue<>();

    public List<Node> getToposortedNodes()
    {
        return toposortedNodes.get(() -> getNodeToposort().stream()
                .flatMap(Set::stream)
                .collect(toImmutableList()));
    }

    private final SupplierLazyValue<List<Node>> reverseToposortedNodes = new SupplierLazyValue<>();

    public List<Node> getReverseToposortedNodes()
    {
        return reverseToposortedNodes.get(() -> getNodeReverseToposort().stream()
                .flatMap(Set::stream)
                .collect(toImmutableList()));
    }

    private final SupplierLazyValue<Map<Node, Integer>> toposortIndicesByNode = new SupplierLazyValue<>();

    public Map<Node, Integer> getToposortIndicesByNode()
    {
        return toposortIndicesByNode.get(() ->
                Streams.zip(
                        IntStream.range(0, nodes.size()).boxed(),
                        getToposortedNodes().stream(),
                        (i, n) -> new Pair.Immutable<>(n, i))
                        .collect(toImmutableMap()));
    }

    private final SupplierLazyValue<Map<Node, Integer>> reverseToposortIndicesByNode = new SupplierLazyValue<>();

    public Map<Node, Integer> getReverseToposortIndicesByNode()
    {
        return reverseToposortIndicesByNode.get(() ->
                Streams.zip(
                        IntStream.range(0, nodes.size()).boxed(),
                        getReverseToposortedNodes().stream(),
                        (i, n) -> new Pair.Immutable<>(n, i))
                        .collect(toImmutableMap()));
    }

    public Map<Node, Set<Node>> getStatefulParentSetsByNode()
    {
        throw new IllegalStateException();
    }

    public Map<Node, Set<Node>> getStatefulChildSetsByNode()
    {
        throw new IllegalStateException();
    }

    public Map<Node, Set<Node>> getStatefulAncestorSetsByNode()
    {
        throw new IllegalStateException();
    }

    public Map<Node, Set<Node>> getStatefulDescendantsSetsByNode()
    {
        throw new IllegalStateException();
    }
}
