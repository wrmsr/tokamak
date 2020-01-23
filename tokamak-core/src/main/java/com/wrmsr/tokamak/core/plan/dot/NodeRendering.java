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

import com.wrmsr.tokamak.core.layout.field.Field;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.util.dot.Color;
import com.wrmsr.tokamak.core.util.dot.DotUtils;
import com.wrmsr.tokamak.util.Cell;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Immutable
class NodeRendering<T extends PNode>
{
    final Class<T> cls;

    final Cell<Color> color = Cell.setOnce();

    public NodeRendering(Class<T> cls)
    {
        this.cls = checkNotNull(cls);
    }

    public Class<T> getCls()
    {
        return cls;
    }

    @Nullable
    public Color getColor()
    {
        return color.isSet() ? color.get() : null;
    }

    public static final class Context<T>
    {
        protected final T node;
        protected final Plan plan;

        public Context(T node, Plan plan)
        {
            this.node = node;
            this.plan = plan;
        }
    }

    public String render(Context<T> ctx)
    {
        DotUtils.Table table = DotUtils.table();

        addHeaderSection(ctx, table);

        addFieldsSection(ctx, table);

        String label = table.render();

        return String.format(
                "\"%s\" [shape=box, style=filled, fillcolor=\"%s\", label=%s];",
                ctx.node.getName(),
                getColor().toString(),
                label);
    }

    protected void addHeaderSection(Context<T> ctx, DotUtils.Table table)
    {
        DotUtils.Section section = DotUtils.section();

        addHeaderName(ctx, section);

        addHeaderId(ctx, section);

        addHeaderAnnotations(ctx, section);

        addHeaderExtra(ctx, section);

        table.add(section);
    }

    protected void addHeaderName(Context<T> ctx, DotUtils.Section section)
    {
        section.add(DotUtils.row(ctx.node.getClass().getSimpleName() + ": " + ctx.node.getName()));
    }

    protected void addHeaderId(Context<T> ctx, DotUtils.Section section)
    {
        section.add(DotUtils.row(ctx.node.getId().toPrefixedString()));
    }

    protected void addHeaderAnnotations(Context<T> ctx, DotUtils.Section section)
    {
        if (!ctx.node.getAnnotations().isEmpty()) {
            DotUtils.Table attsTable = DotUtils.table(
                    DotUtils.section(
                            ctx.node.getAnnotations().stream()
                                    .map(PNodeAnnotation::toDisplayString)
                                    .map(DotUtils::row)
                                    .collect(toImmutableList())));

            section.add(DotUtils.row((DotUtils.rawColumn(DotUtils.render(attsTable::renderInternal)))));
        }
    }

    protected void addHeaderExtra(Context<T> ctx, DotUtils.Section section)
    {

    }

    protected void addFieldsSection(Context<T> ctx, DotUtils.Table table)
    {
        DotUtils.Section section = DotUtils.section();

        ctx.node.getFields().forEach(field -> addFieldRow(ctx, field, section));

        table.add(section);
    }

    protected void addFieldRow(Context<T> ctx, Field field, DotUtils.Section section)
    {
        DotUtils.Row row = DotUtils.row();

        addFieldName(ctx, field, row);

        addFieldExtra(ctx, field, row);

        addFieldType(ctx, field, row);

        addFieldAnnotations(ctx, field, row);

        section.add(row);
    }

    protected void addFieldName(Context<T> ctx, Field field, DotUtils.Row row)
    {
        row.add(DotUtils.column(field.getName()));
    }

    protected void addFieldExtra(Context<T> ctx, Field field, DotUtils.Row row)
    {
    }

    protected void addFieldType(Context<T> ctx, Field field, DotUtils.Row row)
    {
        row.add(DotUtils.column(field.getType().toSpec()));
    }

    protected void addFieldAnnotations(Context<T> ctx, Field field, DotUtils.Row row)
    {
        if (!field.getAnnotations().isEmpty()) {
            DotUtils.Table attsTable = DotUtils.table(
                    DotUtils.section(
                            field.getAnnotations().stream()
                                    .map(FieldAnnotation::toDisplayString)
                                    .map(DotUtils::row)
                                    .collect(toImmutableList())));

            row.add(DotUtils.rawColumn(DotUtils.render(attsTable::renderInternal)));
        }
    }
}
