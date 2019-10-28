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
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MoreCollections.delimitedForEach;

public final class DotUtils
{
    private DotUtils()
    {
    }

    private static final Escaper HTML_ESCAPER = HtmlEscapers.htmlEscaper();

    public static String htmlEscape(String string)
    {
        return HTML_ESCAPER.escape(string);
    }

    public interface Renderable
    {
        void render(StringBuilder sb);

        default String render()
        {
            StringBuilder sb = new StringBuilder();
            render(sb);
            return sb.toString();
        }
    }

    public static final class Column
            implements Renderable
    {
        public static final String DEFAULT_STYLE = "bgcolor=\"white\" align=\"left\"";

        public final String content;
        public final String style;

        private Column(String content, String style)
        {
            this.content = checkNotNull(content);
            this.style = checkNotNull(style);
        }

        private Column(String content)
        {
            this(content, DEFAULT_STYLE);
        }

        @Override
        public void render(StringBuilder sb)
        {
            sb.append("<td ");
            sb.append(style);
            sb.append(">");
            sb.append(content);
            sb.append("</td>");
        }

        private static final Column BLANK = new Column("");
    }

    public static final class Row
            implements Renderable
    {
        public final List<Column> columns = new ArrayList<>();

        private Row add(Column... columns)
        {
            this.columns.addAll(Arrays.asList(columns));
            return this;
        }

        public Row addAll(Iterable<Column> columns)
        {
            this.columns.addAll(ImmutableList.copyOf(columns));
            return this;
        }

        @Override
        public void render(StringBuilder sb)
        {
            columns.forEach(column -> column.render(sb));
        }

        private static final Row BLANK = new Row().add(Column.BLANK);
    }

    public static final class Section
            implements Renderable
    {
        public final List<Row> rows = new ArrayList<>();

        private Section()
        {
        }

        public Section add(Row... rows)
        {
            this.rows.addAll(Arrays.asList(rows));
            return this;
        }

        public Section addAll(Iterable<Row> rows)
        {
            this.rows.addAll(ImmutableList.copyOf(rows));
            return this;
        }

        @Override
        public void render(StringBuilder sb)
        {
            rows.forEach(row -> {
                sb.append("<tr>");
                row.render(sb);
                sb.append("</tr>");
            });
        }

        private static final Section BLANK = new Section().add(Row.BLANK);
    }

    public static final class Table
            implements Renderable
    {
        public final List<Section> sections = new ArrayList<>();

        private Table()
        {
        }

        public Table add(Section... sections)
        {
            this.sections.addAll(Arrays.asList(sections));
            return this;
        }

        public Table addAll(Iterable<Section> sections)
        {
            this.sections.addAll(ImmutableList.copyOf(sections));
            return this;
        }

        public void render(StringBuilder sb)
        {
            sb.append("<<table>");
            delimitedForEach(sections, () -> Section.BLANK.render(sb), section -> section.render(sb));
            sb.append("</table>>");
        }
    }

    public static Column blankColumn()
    {
        return Column.BLANK;
    }

    public static Column column(String content)
    {
        return new Column(htmlEscape(content));
    }

    public static Column column(String content, String style)
    {
        return new Column(htmlEscape(content), style);
    }

    public static Column rawColumn(String content)
    {
        return new Column(content);
    }

    public static Column rawColumn(String content, String style)
    {
        return new Column(content, style);
    }

    public static Row row(Column... columns)
    {
        return new Row().add(columns);
    }

    public static Row row(Iterable<Column> columns)
    {
        return new Row().addAll(columns);
    }

    public static Row row(String... columns)
    {
        return new Row().addAll(Arrays.stream(columns).map(DotUtils::column).collect(toImmutableList()));
    }

    public static Section section(Row... rows)
    {
        return new Section().add(rows);
    }

    public static Section section(Iterable<Row> rows)
    {
        return new Section().addAll(rows);
    }

    public static Table table(Section... sections)
    {
        return new Table().add(sections);
    }

    public static Table table(Iterable<Section> sections)
    {
        return new Table().addAll(sections);
    }
}
