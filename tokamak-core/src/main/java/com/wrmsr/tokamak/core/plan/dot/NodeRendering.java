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
import com.wrmsr.tokamak.core.util.dot.Dot;
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
        Dot.Table table = Dot.table();

        addHeaderSection(ctx, table);

        addFieldsSection(ctx, table);

        String label = table.render();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "\"%s\" [shape=box, style=filled, fillcolor=\"%s\", label=%s];\n",
                ctx.node.getName(),
                checkNotNull(getColor()).toString(),
                label));

        sb.append(renderSources(ctx));

        return sb.toString();
    }

    protected String renderSources(Context<T> ctx)
    {
        StringBuilder sb = new StringBuilder();

        for (PNode source : ctx.node.getSources()) {
            sb.append(renderSource(ctx, source));
        }

        return sb.toString();
    }

    protected String renderSource(Context<T> ctx, PNode source)
    {
        return String.format("\"%s\" -> \"%s\" [dir=back];\n", ctx.node.getName(), source.getName());
    }

    protected void addHeaderSection(Context<T> ctx, Dot.Table table)
    {
        Dot.Section section = Dot.section();

        addHeaderName(ctx, section);

        addHeaderId(ctx, section);

        addHeaderAnnotations(ctx, section);

        addHeaderExtra(ctx, section);

        table.add(section);
    }

    protected void addHeaderName(Context<T> ctx, Dot.Section section)
    {
        section.add(Dot.row(ctx.node.getClass().getSimpleName() + ": " + ctx.node.getName()));
    }

    protected void addHeaderId(Context<T> ctx, Dot.Section section)
    {
        section.add(Dot.row(ctx.node.getId().toPrefixedString()));
    }

    protected void addHeaderAnnotations(Context<T> ctx, Dot.Section section)
    {
        if (!ctx.node.getAnnotations().isEmpty()) {
            Dot.Table attsTable = Dot.table(
                    Dot.section(
                            ctx.node.getAnnotations().stream()
                                    .map(PNodeAnnotation::toDisplayString)
                                    .map(Dot::row)
                                    .collect(toImmutableList())));

            section.add(Dot.row((Dot.rawColumn(Dot.render(attsTable::renderInternal)))));
        }
    }

    protected void addHeaderExtra(Context<T> ctx, Dot.Section section)
    {

    }

    protected void addFieldsSection(Context<T> ctx, Dot.Table table)
    {
        Dot.Section section = Dot.section();

        ctx.node.getFields().forEach(field -> addFieldRow(ctx, field, section));

        table.add(section);
    }

    protected void addFieldRow(Context<T> ctx, Field field, Dot.Section section)
    {
        Dot.Row row = Dot.row();

        addFieldName(ctx, field, row);

        addFieldExtra(ctx, field, row);

        addFieldType(ctx, field, row);

        addFieldAnnotations(ctx, field, row);

        section.add(row);
    }

    protected void addFieldName(Context<T> ctx, Field field, Dot.Row row)
    {
        row.add(Dot.column(field.getName()));
    }

    protected void addFieldExtra(Context<T> ctx, Field field, Dot.Row row)
    {
    }

    protected void addFieldType(Context<T> ctx, Field field, Dot.Row row)
    {
        row.add(Dot.column(field.getType().toSpec()));
    }

    protected void addFieldAnnotations(Context<T> ctx, Field field, Dot.Row row)
    {
        if (!field.getAnnotations().isEmpty()) {
            Dot.Table attsTable = Dot.table(
                    Dot.section(
                            field.getAnnotations().stream()
                                    .map(FieldAnnotation::toDisplayString)
                                    .map(Dot::row)
                                    .collect(toImmutableList())));

            row.add(Dot.rawColumn(Dot.render(attsTable::renderInternal)));
        }
    }
}
