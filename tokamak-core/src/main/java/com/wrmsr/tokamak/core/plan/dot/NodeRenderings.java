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
package com.wrmsr.tokamak.core.plan.dot;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PCrossJoin;
import com.wrmsr.tokamak.core.plan.node.PEquiJoin;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PLookupJoin;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValues;
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

    public static final CtorLazyValue<List<NodeRendering>> RAW_NODE_RENDERINGS = new CtorLazyValue<>(() ->
            ImmutableList.<NodeRendering>builder()
                    .add(new NodeRendering<>(PCache.class))
                    .add(new NodeRendering<>(PCrossJoin.class))
                    .add(new NodeRendering<>(PEquiJoin.class))
                    .add(new NodeRendering<>(PFilter.class))
                    .add(new NodeRendering<>(PGroup.class))
                    .add(new NodeRendering<>(PLookupJoin.class))
                    .add(new NodeRendering<>(PProject.class))
                    .add(new NodeRendering<>(PScan.class))
                    .add(new NodeRendering<>(PState.class))
                    .add(new NodeRendering<>(PUnion.class))
                    .add(new NodeRendering<>(PUnnest.class))
                    .add(new NodeRendering<>(PValues.class))
                    .build());

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

    @SuppressWarnings({"unchecked"})
    public static final CtorLazyValue<Map<Class<? extends PNode>, NodeRendering>> NODE_RENDERING_MAP =
            new CtorLazyValue<>(() -> NODE_RENDERINGS.get().stream().collect(toImmutableMap(NodeRendering::getCls, identity())));
}
