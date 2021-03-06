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

import com.google.common.base.Joiner;
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
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.plan.value.VConstant;
import com.wrmsr.tokamak.core.plan.value.VField;
import com.wrmsr.tokamak.core.plan.value.VFunction;
import com.wrmsr.tokamak.core.plan.value.VNode;
import com.wrmsr.tokamak.core.util.dot.Color;
import com.wrmsr.tokamak.core.util.dot.Dot;
import com.wrmsr.tokamak.util.lazy.CtorLazyValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.Function.identity;

final class NodeRenderings
{
    private NodeRenderings()
    {
    }

    private static final CtorLazyValue<List<NodeRendering<?>>> RAW_NODE_RENDERINGS = new CtorLazyValue<>(() -> ImmutableList.<NodeRendering<?>>builder()

            .add(new NodeRendering<>(PCache.class))

            .add(new NodeRendering<>(PExtract.class))

            .add(new NodeRendering<PFilter>(PFilter.class)
            {
                @Override
                protected void addFieldsSection(Context<PFilter> ctx, Dot.Table table)
                {
                    Dot.Section section = Dot.section();

                    section.add(Dot.row(Dot.rawColumn(" &lt;- " + Dot.escape(ctx.node.getField()))));

                    table.add(section);
                }
            })

            .add(new NodeRendering<>(PGroup.class))

            .add(new NodeRendering<PJoin>(PJoin.class)
            {
                protected String renderSource(Context<PJoin> ctx, PNode source)
                {
                    StringBuilder sb = new StringBuilder();

                    for (PJoin.Branch branch : checkNotEmpty(ctx.node.getBranchSetsByNode().get(source))) {
                        sb.append(String.format(
                                "\"%s\" -> \"%s\" [dir=back, label=\"&nbsp;&nbsp;%s\"];\n",
                                ctx.node.getName(),
                                source.getName(),
                                Dot.escape(Joiner.on(", ").join(branch.getFields()))));
                    }

                    return sb.toString();
                }
            })

            .add(new NodeRendering<>(PLookup.class))

            .add(new NodeRendering<>(POutput.class))

            .add(new NodeRendering<PProject>(PProject.class)
            {
                @Override
                protected void addFieldsSection(Context<PProject> ctx, Dot.Table table)
                {
                    if (ctx.node.getAddedFields().isEmpty() && !ctx.node.getDroppedFields().isEmpty() && ctx.node.getProjection().isForward()) {
                        Dot.Section section = Dot.section();

                        ctx.node.getDroppedFields().forEach(field -> {
                            Dot.Row row = Dot.row();

                            row.add(Dot.column(field));
                            row.add(Dot.rawColumn(" &lt;- ∅"));

                            section.add(row);
                        });

                        table.add(section);
                    }
                    else {
                        super.addFieldsSection(ctx, table);
                    }
                }

                private static final String LEFT_ARROW = " &lt;- ";

                private void addFieldExtra(VNode value, Dot.Row row)
                {
                    checkNotNull(value);
                    if (value instanceof VConstant) {
                        row.add(Dot.rawColumn(LEFT_ARROW + Dot.escape(Objects.toString(((VConstant) value).getValue()))));
                    }
                    else if (value instanceof VField) {
                        row.add(Dot.rawColumn(LEFT_ARROW + Dot.escape(((VField) value).getField())));
                    }
                    else if (value instanceof VFunction) {
                        VFunction functionValue = (VFunction) value;

                        Dot.Table argsTable = Dot.table(Dot.section(
                                functionValue.getArgs().stream()
                                        .map(arg -> {
                                            Dot.Row argRow = Dot.row();
                                            addFieldExtra(arg, argRow);
                                            return argRow;
                                        })
                                        .collect(toImmutableList())));

                        Dot.Table table = Dot.table(
                                Dot.section(Dot.row("λ " + Dot.escape(functionValue.getFunction().getName()))),
                                Dot.section(Dot.row(Dot.rawColumn(Dot.render(argsTable::renderInternal)))));

                        row.add(Dot.rawColumn(Dot.render(table::renderInternal)));
                    }
                    else {
                        row.add(Dot.rawColumn(LEFT_ARROW + "?"));
                    }
                }

                @Override
                protected void addFieldExtra(Context<PProject> ctx, Field field, Dot.Row row)
                {
                    addFieldExtra(ctx.node.getProjection().getInputsByOutput().get(field.getName()), row);
                }
            })

            .add(new NodeRendering<PScan>(PScan.class)
            {
                @Override
                protected void addHeaderSection(Context<PScan> ctx, Dot.Table table)
                {
                    super.addHeaderSection(ctx, table);

                    table.add(Dot.section(Dot.row(ctx.node.getSchemaTable().toDotString())));
                }
            })

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
