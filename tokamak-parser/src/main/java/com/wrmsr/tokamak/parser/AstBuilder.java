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
package com.wrmsr.tokamak.parser;

import com.wrmsr.tokamak.parser.tree.AllSelectItem;
import com.wrmsr.tokamak.parser.tree.Expression;
import com.wrmsr.tokamak.parser.tree.ExpressionSelectItem;
import com.wrmsr.tokamak.parser.tree.IntegerLiteral;
import com.wrmsr.tokamak.parser.tree.NullLiteral;
import com.wrmsr.tokamak.parser.tree.QualifiedName;
import com.wrmsr.tokamak.parser.tree.Select;
import com.wrmsr.tokamak.parser.tree.SelectItem;
import com.wrmsr.tokamak.parser.tree.StringLiteral;
import com.wrmsr.tokamak.parser.tree.TreeNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public class AstBuilder
{
    public AstBuilder()
    {

    }

    public TreeNode build(ParseTree parseTree)
    {
        return parseTree.accept(new SqlBaseVisitor<TreeNode>()
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
            public TreeNode visitSelectAll(SqlParser.SelectAllContext ctx)
            {
                return new AllSelectItem();
            }

            @Override
            public TreeNode visitSelectExpression(SqlParser.SelectExpressionContext ctx)
            {
                return new ExpressionSelectItem((Expression) visit(ctx.expression()));
            }

            @Override
            public TreeNode visitSingleStatement(SqlParser.SingleStatementContext ctx)
            {
                return visit(ctx.statement());
            }

            @Override
            public TreeNode visitIntegerLiteral(SqlParser.IntegerLiteralContext ctx)
            {
                return new IntegerLiteral(Long.parseLong(ctx.getText()));
            }

            @Override
            public TreeNode visitNullLiteral(SqlParser.NullLiteralContext ctx)
            {
                return new NullLiteral();
            }

            @Override
            public TreeNode visitStringLiteral(SqlParser.StringLiteralContext ctx)
            {
                return new StringLiteral(ctx.STRING_VALUE().getText());
            }

            @Override
            public TreeNode visitQualifiedName(SqlParser.QualifiedNameContext ctx)
            {
                visit(ctx.identifier()
                return new QualifiedName();
            }

            // @Override
            // public TreeNode visitTableName(SqlParser.TableNameContext ctx)
            // {
            //     return new TableName((QualifiedName) visit(ctx.qualifiedName()));
            // }
        });
    }
}
