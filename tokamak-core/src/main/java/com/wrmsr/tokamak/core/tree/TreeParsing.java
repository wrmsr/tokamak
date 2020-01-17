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

import com.wrmsr.tokamak.core.parse.SqlBaseVisitor;
import com.wrmsr.tokamak.core.parse.SqlLexer;
import com.wrmsr.tokamak.core.parse.SqlParser;
import com.wrmsr.tokamak.core.tree.node.TAliasedRelation;
import com.wrmsr.tokamak.core.tree.node.TAllSelectItem;
import com.wrmsr.tokamak.core.tree.node.TBooleanExpression;
import com.wrmsr.tokamak.core.tree.node.TComparisonExpression;
import com.wrmsr.tokamak.core.tree.node.TExpression;
import com.wrmsr.tokamak.core.tree.node.TExpressionSelectItem;
import com.wrmsr.tokamak.core.tree.node.TFunctionCallExpression;
import com.wrmsr.tokamak.core.tree.node.TIdentifier;
import com.wrmsr.tokamak.core.tree.node.TNode;
import com.wrmsr.tokamak.core.tree.node.TNotExpression;
import com.wrmsr.tokamak.core.tree.node.TNullLiteral;
import com.wrmsr.tokamak.core.tree.node.TNumberLiteral;
import com.wrmsr.tokamak.core.tree.node.TQualifiedName;
import com.wrmsr.tokamak.core.tree.node.TQualifiedNameExpression;
import com.wrmsr.tokamak.core.tree.node.TRelation;
import com.wrmsr.tokamak.core.tree.node.TSelect;
import com.wrmsr.tokamak.core.tree.node.TSelectItem;
import com.wrmsr.tokamak.core.tree.node.TStringLiteral;
import com.wrmsr.tokamak.core.tree.node.TSubqueryRelation;
import com.wrmsr.tokamak.core.tree.node.TTableName;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class TreeParsing
{
    private TreeParsing()
    {
    }

    public static SqlParser parse(String str)
    {
        CharStream input = new CaseInsensitiveCharStream(CharStreams.fromString(str));
        SqlLexer lexer = new SqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new SqlParser(tokens);
    }

    public static TNode build(ParseTree tree)
    {
        return build(tree, new ParseOptions());
    }

    public static TNode build(ParseTree tree, ParseOptions options)
    {
        return tree.accept(new SqlBaseVisitor<TNode>()
        {
            @Override
            protected TNode defaultResult()
            {
                return super.defaultResult();
            }

            @Override
            protected TNode aggregateResult(TNode aggregate, TNode nextResult)
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
            public TNode visitAliasedRelation(SqlParser.AliasedRelationContext ctx)
            {
                return new TAliasedRelation(
                        (TRelation) visit(ctx.relation()),
                        ctx.identifier() != null ? Optional.of(ctx.identifier().getText()) : Optional.empty());
            }

            @Override
            public TNode visitBooleanExpression(SqlParser.BooleanExpressionContext ctx)
            {
                return new TBooleanExpression(
                        (TExpression) visit(ctx.left),
                        TBooleanExpression.Op.fromString(ctx.booleanOperator().getText()),
                        (TExpression) visit(ctx.right));
            }

            @Override
            public TNode visitComparisonExpression(SqlParser.ComparisonExpressionContext ctx)
            {
                return new TComparisonExpression(
                        (TExpression) visit(ctx.left),
                        TComparisonExpression.Op.fromString(ctx.comparisonOperator().getText()),
                        (TExpression) visit(ctx.right));
            }

            @Override
            public TNode visitFunctionCallExpression(SqlParser.FunctionCallExpressionContext ctx)
            {
                return new TFunctionCallExpression(
                        ((TIdentifier) visit(ctx.identifier())).getValue(),
                        visit(ctx.expression(), TExpression.class));
            }

            @Override
            public TNode visitNotExpression(SqlParser.NotExpressionContext ctx)
            {
                return new TNotExpression(
                        (TExpression) visit(ctx.expression()));
            }

            @Override
            public TNode visitNumberLiteral(SqlParser.NumberLiteralContext ctx)
            {
                return new TNumberLiteral(Long.parseLong(ctx.getText()));
            }

            @Override
            public TNode visitNullLiteral(SqlParser.NullLiteralContext ctx)
            {
                return new TNullLiteral();
            }

            @Override
            public TNode visitParenthesizedExpression(SqlParser.ParenthesizedExpressionContext ctx)
            {
                return (TExpression) visit(ctx.expression());
            }

            @Override
            public TNode visitQualifiedName(SqlParser.QualifiedNameContext ctx)
            {
                List<String> parts = visit(ctx.identifier(), TIdentifier.class).stream()
                        .map(TIdentifier::getValue)
                        .collect(Collectors.toList());
                return new TQualifiedName(parts);
            }

            @Override
            public TNode visitQualifiedNameExpression(SqlParser.QualifiedNameExpressionContext ctx)
            {
                return new TQualifiedNameExpression(
                        (TQualifiedName) visit(ctx.qualifiedName()));
            }

            @Override
            public TNode visitQuotedIdentifier(SqlParser.QuotedIdentifierContext ctx)
            {
                String text = ctx.getText();
                return new TIdentifier(text.substring(1, text.length() - 1));
            }

            @Override
            public TNode visitUnquotedIdentifier(SqlParser.UnquotedIdentifierContext ctx)
            {
                return new TIdentifier(ctx.getText());
            }

            @Override
            public TNode visitSelect(SqlParser.SelectContext ctx)
            {
                List<TSelectItem> selectItems = visit(ctx.selectItem(), TSelectItem.class);
                List<TAliasedRelation> relations = visit(ctx.aliasedRelation(), TAliasedRelation.class);
                Optional<TExpression> where = ctx.where != null ? Optional.of((TExpression) visit(ctx.where)) : Optional.empty();
                return new TSelect(
                        selectItems,
                        relations,
                        where);
            }

            @Override
            public TNode visitSelectAll(SqlParser.SelectAllContext ctx)
            {
                return new TAllSelectItem();
            }

            @Override
            public TNode visitSelectExpression(SqlParser.SelectExpressionContext ctx)
            {
                return new TExpressionSelectItem(
                        (TExpression) visit(ctx.expression()),
                        ctx.identifier() != null ? Optional.of(ctx.identifier().getText()) : Optional.empty());
            }

            @Override
            public TNode visitSingleQuotedStringLiteral(SqlParser.SingleQuotedStringLiteralContext ctx)
            {
                String raw = ctx.getText();
                checkState(raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'"));
                if (!options.isTwoQuotesAsEscapedQuote() && raw.length() >= 6 && raw.startsWith("'''") && raw.endsWith("'''")) {
                    raw = raw.substring(3, raw.length() - 3);
                }
                else {
                    raw = raw.substring(1, raw.length() - 1);
                }
                if (options.isTwoQuotesAsEscapedQuote()) {
                    raw = raw.replaceAll("''", "'");
                }
                return new TStringLiteral(TreeStrings.escaped(raw));
            }

            @Override
            public TNode visitSingleStatement(SqlParser.SingleStatementContext ctx)
            {
                return visit(ctx.statement());
            }

            @Override
            public TNode visitSubqueryRelation(SqlParser.SubqueryRelationContext ctx)
            {
                return new TSubqueryRelation((TSelect) visit(ctx.select()));
            }

            @Override
            public TNode visitTableName(SqlParser.TableNameContext ctx)
            {
                return new TTableName((TQualifiedName) visit(ctx.qualifiedName()));
            }

            @Override
            public TNode visitTripleQuotedStringLiteral(SqlParser.TripleQuotedStringLiteralContext ctx)
            {
                String raw = ctx.getText();
                checkState(raw.length() >= 6 && raw.startsWith("'''") && raw.endsWith("'''"));
                raw = raw.substring(3, raw.length() - 3);
                return new TStringLiteral(TreeStrings.escaped(raw));
            }
        });
    }
}
