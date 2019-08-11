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
import com.wrmsr.tokamak.parser.tree.TreeNode;
import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class ParseTest
        extends TestCase
{
    public void testParse()
            throws Throwable
    {
        for (String str : new String[] {
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

            TreeNode treeNode = tree.accept(new SqlBaseVisitor<TreeNode>()
            {
                @Override
                public TreeNode visitStatement(SqlParser.StatementContext ctx)
                {
                    return super.visitStatement(ctx);
                }

                @Override
                public TreeNode visitQuery(SqlParser.QueryContext ctx)
                {
                    return super.visitQuery(ctx);
                }

                @Override
                public TreeNode visitSelectItem(SqlParser.SelectItemContext ctx)
                {
                    return super.visitSelectItem(ctx);
                }

                @Override
                public TreeNode visitRelation(SqlParser.RelationContext ctx)
                {
                    return super.visitRelation(ctx);
                }

                @Override
                public TreeNode visitQualifiedName(SqlParser.QualifiedNameContext ctx)
                {
                    return super.visitQualifiedName(ctx);
                }

                @Override
                public TreeNode visitUnquotedIdentifier(SqlParser.UnquotedIdentifierContext ctx)
                {
                    return super.visitUnquotedIdentifier(ctx);
                }

                @Override
                public TreeNode visitQuotedIdentifier(SqlParser.QuotedIdentifierContext ctx)
                {
                    return super.visitQuotedIdentifier(ctx);
                }

                @Override
                public TreeNode visitExpression(SqlParser.ExpressionContext ctx)
                {
                    return super.visitExpression(ctx);
                }

                @Override
                public TreeNode visitBooleanExpression(SqlParser.BooleanExpressionContext ctx)
                {
                    return super.visitBooleanExpression(ctx);
                }

                @Override
                public TreeNode visitPrimaryExpression(SqlParser.PrimaryExpressionContext ctx)
                {
                    return super.visitPrimaryExpression(ctx);
                }
            });
        }
    }
}
