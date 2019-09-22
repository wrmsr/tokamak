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
package com.wrmsr.tokamak.core.plan.node.visitor;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.plan.node.Node;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public final class NodeVisitors
{
    private NodeVisitors()
    {
    }

    public static <C, R> void preWalk(Node node, NodeVisitor<R, C> visitor, C context)
    {
        node.accept(visitor, context);
        for (Node child : node.getSources()) {
            preWalk(child, visitor, context);
        }
    }

    public static <C, R> void postWalk(Node node, NodeVisitor<R, C> visitor, C context)
    {
        for (Node child : node.getSources()) {
            postWalk(child, visitor, context);
        }
        node.accept(visitor, context);
    }

    public static List<Node> linearize(Node node)
    {
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        Set<Node> seen = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            Node cur = queue.remove();
            checkState(!seen.contains(node));
            builder.add(cur);
            for (Node child : cur.getSources()) {
                if (!seen.contains(child)) {
                    queue.add(child);
                    seen.add(child);
                }
            }
        }
        return builder.build();
    }

    public static <C, R> void acceptAll(Node root, NodeVisitor<R, C> visitor, C context)
    {
        linearize(root).forEach(n -> n.accept(visitor, context));
    }

    public static <C, R> void cacheAll(Node root, CachingNodeVisitor<R, C> visitor, C context)
    {
        linearize(root).forEach(n -> visitor.get(n, context));
    }
}
