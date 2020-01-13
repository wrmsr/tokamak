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
package com.wrmsr.tokamak.core.tree;

import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TStringLiteral;
import com.wrmsr.tokamak.core.tree.transform.SearchFunctionParsing;
import junit.framework.TestCase;

public class ParserTest
        extends TestCase
{
    private static String getSelectItemText(TNode node)
    {
        return ((TStringLiteral) ((TExpressionSelectItem) ((TSelect) node).getItems().get(0)).getExpression()).getValue().getValue();
    }

    public void testParse()
            throws Throwable
    {
        for (String str : new String[] {
                // "select *",
                // "select 420",
                // "select /*+ hint */ 1",
                // "select 'hu'",
                // "select a",
                // "select a from a",
                // "select a() as c, d from e",
                // "select a(b) as c, d from e",
                // "select a(b, x) as c, d from e",
                // "select a // comment",
                // "select a -- comment",
                "select 'hi' a",
                "select 'hi\\\'' a",
                "select '''hi''' a",
                "select '''hi'there''' a",
                "select ''''hi'''' a",
        }) {
            System.out.println(str);
            SqlParser parser = TreeParsing.parse(str);
            System.out.println(parser);
            TNode node = TreeParsing.build(parser.statement());
            System.out.println(getSelectItemText(node));
        }
    }

    public void testParseSearch()
            throws Throwable
    {
        for (String str : new String[] {
                // "select *",
                // "select 420",
                // "select /*+ hint */ 1",
                // "select 'hu'",
                // "select a",
                // "select a from a",
                // "select a() as c, d from e",
                // "select a(b) as c, d from e",
                // "select a(b, x) as c, d from e",
                // "select a // comment",
                // "select a -- comment",
                "select search('hi') ",
                "select search('{hi: there, my: dogg}') ",
                "select search('a.b.c[]') ",
                "select search('a.b.c.d.e.f[]') ",
                "select search('''locations[?state == 'WA'].name | sort(@)[-2:] | {WashingtonCities: join(', ', @)}''') ",
        }) {
            System.out.println(str);
            SqlParser parser = TreeParsing.parse(str);
            System.out.println(parser);
            TNode node = TreeParsing.build(parser.statement());
            node = SearchFunctionParsing.parseSearches(node);
            System.out.println(node);
        }
    }

    public void testParseSubquery()
            throws Throwable
    {
        for (String str : new String[] {
                "select a from (select a from b) b",
        }) {
            System.out.println(str);
            SqlParser parser = TreeParsing.parse(str);
            System.out.println(parser);
            TNode node = TreeParsing.build(parser.statement());
            System.out.println(TreeRendering.render(node));
        }
    }

    public void testParseQuoteMode()
            throws Throwable
    {
        for (String str : new String[] {
                "select 'hi' a",
                "select 'h''i' a",
                "select 'h\\''i' a",
                "select '''hi''' a",
                "select '''hi'there''' a",
                "select ''''h''i''' a",
                "select ''''h\\''i''' a",
                "select ''''''' a",
                "select '''''''' a",
        }) {
            System.out.println(str);
            for (boolean mode : new boolean[] {false, true}) {
                System.out.println(mode);
                SqlParser parser = TreeParsing.parse(str);
                TNode node = TreeParsing.build(parser.statement(), ParseOptions.builder().twoQuotesAsEscapedQuote(mode).build());
                System.out.println(getSelectItemText(node));
            }
            System.out.println();
        }
    }

    public void testGarbage()
            throws Throwable
    {
        for (String str : new String[] {
                "select 'hi' a a a",
        }) {
            System.out.println(str);
            SqlParser parser = TreeParsing.parse(str);
            TNode node = TreeParsing.build(parser.statement());
            System.out.println(node);
        }
    }
}
