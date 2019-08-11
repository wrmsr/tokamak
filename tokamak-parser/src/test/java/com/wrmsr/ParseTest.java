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
package com.wrmsr;

import com.wrmsr.tokamak.parser.CaseInsensitiveCharStream;
import com.wrmsr.tokamak.parser.SqlBaseVisitor;
import com.wrmsr.tokamak.parser.SqlLexer;
import com.wrmsr.tokamak.parser.SqlParser;
import com.wrmsr.tokamak.parser.tree.AllSelectItem;
import com.wrmsr.tokamak.parser.tree.Select;
import com.wrmsr.tokamak.parser.tree.SelectItem;
import com.wrmsr.tokamak.parser.tree.TreeNode;
import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public class ParseTest
        extends TestCase
{
    public void testParse()
            throws Throwable
    {
        for (String str : new String[] {
                "select *",
                "select 420",
                "select /*+ hint */ 1",
                "select a",
                "select a from a",
        }) {
            CharStream input = new CaseInsensitiveCharStream(CharStreams.fromString(str));

            SqlLexer lexer = new SqlLexer(input);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            SqlParser parser = new SqlParser(tokens);

            ParseTree tree = parser.statement();
            System.out.println(tree.toStringTree(parser));

            TreeNode node = tree.accept(new SqlBaseVisitor<TreeNode>()
            {
                @Override
                protected TreeNode defaultResult()
                {
                    return super.defaultResult();
                }

                @Override
                protected TreeNode aggregateResult(TreeNode aggregate, TreeNode nextResult)
                {
                    checkState(aggregate == null);
                    return checkNotNull(nextResult);
                }

                private <T> List<T> visit(List<? extends ParserRuleContext> contexts, Class<T> cls)
                {
                    return contexts.stream()
                            .map(this::visit)
                            .map(cls::cast)
                            .collect(toImmutableList());
                }

                @Override
                public TreeNode visitSelect(SqlParser.SelectContext ctx)
                {
                    List<SelectItem> selectItems = visit(ctx.selectItem(), SelectItem.class);
                    return new Select(
                            selectItems);
                }

                @Override
                public TreeNode visitSelectExpression(SqlParser.SelectExpressionContext ctx)
                {
                    return super.visitSelectExpression(ctx);
                }

                @Override
                public TreeNode visitSelectAll(SqlParser.SelectAllContext ctx)
                {
                    return new AllSelectItem();
                }
            });

            System.out.println(node);
        }
    }
}
