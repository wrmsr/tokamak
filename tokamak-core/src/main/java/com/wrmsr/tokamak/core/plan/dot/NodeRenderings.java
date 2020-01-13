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
import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.plan.node.PCache;
import com.wrmsr.tokamak.core.plan.node.PExtract;
import com.wrmsr.tokamak.core.plan.node.PFilter;
import com.wrmsr.tokamak.core.plan.node.PGroup;
import com.wrmsr.tokamak.core.plan.node.PJoin;
import com.wrmsr.tokamak.core.plan.node.PLookup;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.POutput;
import com.wrmsr.tokamak.core.plan.node.PProject;
import com.wrmsr.tokamak.core.plan.node.PScan;
import com.wrmsr.tokamak.core.plan.node.PScope;
import com.wrmsr.tokamak.core.plan.node.PScopeExit;
import com.wrmsr.tokamak.core.plan.node.PSearch;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.PStruct;
import com.wrmsr.tokamak.core.plan.node.PUnify;
import com.wrmsr.tokamak.core.plan.node.PUnion;
import com.wrmsr.tokamak.core.plan.node.PUnnest;
import com.wrmsr.tokamak.core.plan.node.PValue;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.util.lazy.CtorLazyValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static java.util.function.Function.identity;

public final class NodeRenderings
{
    private NodeRenderings()
    {
    }

    private static final CtorLazyValue<List<NodeRendering<?>>> RAW_NODE_RENDERINGS = new CtorLazyValue<>(() -> ImmutableList.<NodeRendering<?>>builder()

            .add(new NodeRendering<>(PCache.class))

            .add(new NodeRendering<>(PExtract.class))

            .add(new NodeRendering<>(PFilter.class))

            .add(new NodeRendering<>(PGroup.class))

            .add(new NodeRendering<>(PJoin.class))

            .add(new NodeRendering<>(PLookup.class))

            .add(new NodeRendering<>(POutput.class))

            .add(new NodeRendering<PProject>(PProject.class)
            {
                @Override
                protected void addFieldsSection(Context<PProject> ctx, DotUtils.Table table)
                {
                    if (ctx.node.getAddedFields().isEmpty() && !ctx.node.getDroppedFields().isEmpty() && ctx.node.getProjection().isForward()) {
                        DotUtils.Section section = DotUtils.section();

                        ctx.node.getDroppedFields().forEach(field -> {
                            DotUtils.Row row = DotUtils.row();

                            row.add(DotUtils.column(field));
                            row.add(DotUtils.rawColumn(" &lt;- ∅"));

                            section.add(row);
                        });

                        table.add(section);
                    }
                    else {
                        super.addFieldsSection(ctx, table);
                    }
                }

                @Override
                protected void addFieldExtra(Context<PProject> ctx, Field field, DotUtils.Row row)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" &lt;- ");

                    PValue value = checkNotNull(ctx.node.getProjection().getInputsByOutput().get(field.getName()));
                    if (value instanceof PValue.Constant) {
                        sb.append(DotUtils.escape(Objects.toString(((PValue.Constant) value).getValue())));
                    }
                    else if (value instanceof PValue.Field) {
                        sb.append(DotUtils.escape(((PValue.Field) value).getField()));
                    }
                    else if (value instanceof PValue.Function) {
                        sb.append("λ ").append(DotUtils.escape(((PValue.Function) value).getFunction().getName()));
                    }
                    else {
                        sb.append("?");
                    }

                    row.add(DotUtils.rawColumn(sb.toString()));
                }
            })

            .add(new NodeRendering<>(PScan.class))

            .add(new NodeRendering<>(PScope.class))

            .add(new NodeRendering<>(PScopeExit.class))

            .add(new NodeRendering<>(PSearch.class))

            .add(new NodeRendering<>(PState.class))

            .add(new NodeRendering<>(PStruct.class))

            .add(new NodeRendering<>(PUnify.class))

            .add(new NodeRendering<>(PUnion.class))

            .add(new NodeRendering<>(PUnnest.class))

            .add(new NodeRendering<>(PValues.class))

            .build());

    public static final CtorLazyValue<List<NodeRendering<?>>> NODE_RENDERINGS = new CtorLazyValue<>(() -> {
        List<NodeRendering<?>> renderings = RAW_NODE_RENDERINGS.get();

        List<Color> colors = Color.RAINBOW.get();
        int step = (int) ((float) colors.size() / renderings.stream().filter(r -> !r.color.isSet()).count());

        for (int i = 0, j = 0; i < renderings.size(); ++i) {
            NodeRendering<?> rendering = renderings.get(i);
            if (!rendering.color.isSet()) {
                rendering.color.set(colors.get((j++) * step));
            }
        }

        return renderings;
    });

    public static final CtorLazyValue<Map<Class<? extends PNode>, NodeRendering<?>>> NODE_RENDERING_MAP = new CtorLazyValue<>(() ->
            NODE_RENDERINGS.get().stream().collect(toImmutableMap(NodeRendering::getCls, identity())));
}
