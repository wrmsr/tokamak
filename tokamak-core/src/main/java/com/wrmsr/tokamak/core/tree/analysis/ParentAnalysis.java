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
package com.wrmsr.tokamak.core.tree.analysis;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.visitor.TraversalTNodeVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.wrmsr.tokamak.util.MoreCollections.newImmutableSetMap;

public final class ParentAnalysis
{
    private final Map<TNode, Set<TNode>> parentNodeSetsByNode;

    private ParentAnalysis(Map<TNode, Set<TNode>> parentNodeSetsByNode)
    {
        this.parentNodeSetsByNode = newImmutableSetMap(parentNodeSetsByNode);
    }

    public Map<TNode, Set<TNode>> getParentNodeSetsByNode()
    {
        return parentNodeSetsByNode;
    }

    public Set<TNode> getParents(TNode node)
    {
        return parentNodeSetsByNode.getOrDefault(node, ImmutableSet.of());
    }

    public static ParentAnalysis analyzeParents(TNode root)
    {
        Map<TNode, Set<TNode>> ret = new HashMap<>();

        new TraversalTNodeVisitor<Void, Optional<TNode>>()
        {
            @Override
            protected Void visitNode(TNode node, Optional<TNode> parent)
            {
                parent.ifPresent(p -> ret.computeIfAbsent(node, (n) -> new HashSet<>()).add(p));

                return super.visitNode(node, Optional.of(node));
            }
        }.process(root, Optional.empty());

        return new ParentAnalysis(ret);
    }
}
