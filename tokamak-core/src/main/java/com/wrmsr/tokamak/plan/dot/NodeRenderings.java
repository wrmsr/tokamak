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
package com.wrmsr.tokamak.plan.dot;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.plan.node.CacheNode;
import com.wrmsr.tokamak.plan.node.CrossJoinNode;
import com.wrmsr.tokamak.plan.node.EquijoinNode;
import com.wrmsr.tokamak.plan.node.FilterNode;
import com.wrmsr.tokamak.plan.node.ListAggregateNode;
import com.wrmsr.tokamak.plan.node.LookupJoinNode;
import com.wrmsr.tokamak.plan.node.Node;
import com.wrmsr.tokamak.plan.node.PersistNode;
import com.wrmsr.tokamak.plan.node.ProjectNode;
import com.wrmsr.tokamak.plan.node.ScanNode;
import com.wrmsr.tokamak.plan.node.UnionNode;
import com.wrmsr.tokamak.plan.node.UnnestNode;
import com.wrmsr.tokamak.plan.node.ValuesNode;
import com.wrmsr.tokamak.util.lazy.CtorLazyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public final class NodeRenderings
{
    private NodeRenderings()
    {
    }

    public static final CtorLazyValue<List<NodeRendering>> RAW_NODE_RENDERINGS = new CtorLazyValue<>(() -> {
        return ImmutableList.<NodeRendering>builder()
                .add(new NodeRendering<>(CacheNode.class))
                .add(new NodeRendering<>(CrossJoinNode.class))
                .add(new NodeRendering<>(EquijoinNode.class))
                .add(new NodeRendering<>(FilterNode.class))
                .add(new NodeRendering<>(ListAggregateNode.class))
                .add(new NodeRendering<>(LookupJoinNode.class))
                .add(new NodeRendering<>(PersistNode.class))
                .add(new NodeRendering<>(ProjectNode.class))
                .add(new NodeRendering<>(ScanNode.class))
                .add(new NodeRendering<>(UnionNode.class))
                .add(new NodeRendering<>(UnnestNode.class))
                .add(new NodeRendering<>(ValuesNode.class))
                .build();
    });

    @SuppressWarnings({"unchecked"})
    public static final CtorLazyValue<List<NodeRendering>> NODE_RENDERINGS = new CtorLazyValue<>(() -> {
        List<NodeRendering> renderings = new ArrayList<>(RAW_NODE_RENDERINGS.get());

        List<Color> colors = Color.RAINBOW.get();
        int step = (int) ((float) colors.size() / renderings.stream().filter(r -> r.getColor() == null).count());
        for (int i = 0, j = 0; i < renderings.size(); ++i) {
            if (renderings.get(i).getColor() == null) {
                renderings.set(i, new NodeRendering(renderings.get(i), colors.get((j++) * step)));
            }
        }

        return ImmutableList.copyOf(renderings);
    });

    public static final CtorLazyValue<Map<Class<? extends Node>, NodeRendering>> NODE_RENDERING_MAP =
            new CtorLazyValue<>(() -> NODE_RENDERINGS.get().stream().collect(toImmutableMap(NodeRendering::getCls, identity())));
}
