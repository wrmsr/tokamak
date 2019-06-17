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
package com.wrmsr.tokamak.materialization.plan;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.materialization.api.NodeId;
import com.wrmsr.tokamak.materialization.api.NodeName;
import com.wrmsr.tokamak.materialization.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public final class Plan
{
    private final Node root;

    private final Set<Node> nodes;
    private final Map<NodeName, Node> nodesByName;
    private final Map<NodeId, Node> nodesByNodeId;

    public Plan(Node root)
    {
        this.root = root;

        Set<Node> nodes = new HashSet<>();
        Map<NodeName, Node> nodesByName = new HashMap<>();
        Map<NodeId, Node> nodesByNodeId = new HashMap<>();

        List<Node> nodeStack = new ArrayList<>();
        nodeStack.add(root);
        while (!nodeStack.isEmpty()) {
            Node cur = nodeStack.remove(nodeStack.size() - 1);
            checkState(!nodes.contains(cur));
            checkArgument(!nodesByName.containsKey(cur.getName()));
            checkArgument(!nodesByNodeId.containsKey(cur.getNodeId()));
            nodes.add(cur);
            nodesByName.put(cur.getName(), cur);
            nodesByNodeId.put(cur.getNodeId(), cur);
            cur.getChildren().stream()
                    .filter(n -> !nodes.contains(n))
                    .forEach(nodeStack::add);
        }

        this.nodes = ImmutableSet.copyOf(nodes);
        this.nodesByName = ImmutableMap.copyOf(nodesByName);
        this.nodesByNodeId = ImmutableMap.copyOf(nodesByNodeId);
    }

    public Node getRoot()
    {
        return root;
    }

    public Set<Node> getNodes()
    {
        return nodes;
    }

    public Map<NodeName, Node> getNodesByName()
    {
        return nodesByName;
    }

    public Map<NodeId, Node> getNodesByNodeId()
    {
        return nodesByNodeId;
    }

    public List<Node> getNameSortedNodes()
    {
        throw new IllegalStateException();
    }

    public Map<Node, Integer> getNameSortedIndicesByNode()
    {
        throw new IllegalStateException();
    }

    public List<Node> getNodeTypeList(Class<? extends Node> nodeType)
    {
        throw new IllegalStateException();
    }

    public Set<Node> getLeafNodes()
    {
        throw new IllegalStateException();
    }

    public List<List<Node>> getNodeToposort()
    {
        throw new IllegalStateException();
    }

    public List<List<Node>> getNodeReverseToposort()
    {
        throw new IllegalStateException();
    }

    public Map<Node, Integer> getToposortIndicesByNode()
    {
        throw new IllegalStateException();
    }

    public Map<Node, Integer> getReverseToposortIndicesByNode()
    {
        throw new IllegalStateException();
    }

    public List<Node> getToposortedNodes()
    {
        throw new IllegalStateException();
    }

    public List<Node> getReverseToposortedNodes()
    {
        throw new IllegalStateException();
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
