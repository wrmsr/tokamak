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
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public final class PNodeVisitors
{
    private PNodeVisitors()
    {
    }

    public static <C, R> void preWalk(PNode node, PNodeVisitor<R, C> visitor, C context)
    {
        visitor.process(node, context);
        for (PNode child : node.getSources()) {
            preWalk(child, visitor, context);
        }
    }

    public static <C, R> void postWalk(PNode node, PNodeVisitor<R, C> visitor, C context)
    {
        for (PNode child : node.getSources()) {
            postWalk(child, visitor, context);
        }
        visitor.process(node, context);
    }

    public static List<PNode> linearize(PNode node)
    {
        ImmutableList.Builder<PNode> builder = ImmutableList.builder();
        Set<PNode> seen = new HashSet<>();
        Queue<PNode> queue = new ArrayDeque<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            PNode cur = queue.remove();
            checkState(!seen.contains(node));
            builder.add(cur);
            for (PNode child : cur.getSources()) {
                if (!seen.contains(child)) {
                    queue.add(child);
                    seen.add(child);
                }
            }
        }
        return builder.build();
    }

    public static <C, R> void acceptAll(PNode root, PNodeVisitor<R, C> visitor, C context)
    {
        linearize(root).forEach(n -> visitor.process(n, context));
    }
}
