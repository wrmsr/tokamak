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
package com.wrmsr.tokamak.core.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.parse.SearchBaseVisitor;
import com.wrmsr.tokamak.core.parse.SearchLexer;
import com.wrmsr.tokamak.core.parse.SearchParser;
import com.wrmsr.tokamak.core.search.node.SAnd;
import com.wrmsr.tokamak.core.search.node.SCmp;
import com.wrmsr.tokamak.core.search.node.SComparison;
import com.wrmsr.tokamak.core.search.node.SCreateArray;
import com.wrmsr.tokamak.core.search.node.SCreateObject;
import com.wrmsr.tokamak.core.search.node.SCurrent;
import com.wrmsr.tokamak.core.search.node.SExpressionRef;
import com.wrmsr.tokamak.core.search.node.SFlattenArray;
import com.wrmsr.tokamak.core.search.node.SFlattenObject;
import com.wrmsr.tokamak.core.search.node.SFunctionCall;
import com.wrmsr.tokamak.core.search.node.SIndex;
import com.wrmsr.tokamak.core.search.node.SJsonLiteral;
import com.wrmsr.tokamak.core.search.node.SNegate;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.core.search.node.SOr;
import com.wrmsr.tokamak.core.search.node.SProject;
import com.wrmsr.tokamak.core.search.node.SProperty;
import com.wrmsr.tokamak.core.search.node.SSelection;
import com.wrmsr.tokamak.core.search.node.SSequence;
import com.wrmsr.tokamak.core.search.node.SSlice;
import com.wrmsr.tokamak.core.search.node.SString;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class SearchParsing
{
    private SearchParsing()
    {
    }

    public static SearchParser parse(String str)
    {
        CharStream input = CharStreams.fromString(str);
        SearchLexer lexer = new SearchLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new SearchParser(tokens);
    }

    public static SNode build(ParseTree tree)
    {
        return tree.accept(new SearchBaseVisitor<SNode>()
        {
            private SNode chainedNode;

            private SNode createProjectionIfChained(SNode node)
            {
                if (chainedNode != null) {
                    node = new SSequence(ImmutableList.of(node, new SProject(chainedNode)));
                    chainedNode = null;
                }
                return node;
            }

            private SNode createSequenceIfChained(SNode node)
            {
                if (chainedNode != null) {
                    node = new SSequence(ImmutableList.of(node, chainedNode));
                    chainedNode = null;
                }
                return node;
            }

            private SNode nonChainingVisit(ParseTree tree)
            {
                SNode stashedNextNode = chainedNode;
                chainedNode = null;
                SNode result = createSequenceIfChained(visit(tree));
                chainedNode = stashedNextNode;
                return result;
            }

            @Override
            protected SNode defaultResult()
            {
                return super.defaultResult();
            }

            @Override
            protected SNode aggregateResult(SNode aggregate, SNode nextResult)
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
            public SNode visitAndExpression(SearchParser.AndExpressionContext ctx)
            {
                SNode left = nonChainingVisit(ctx.expression(0));
                SNode right = nonChainingVisit(ctx.expression(1));
                return createSequenceIfChained(new SAnd(left, right));
            }

            @Override
            public SNode visitBracketExpression(SearchParser.BracketExpressionContext ctx)
            {
                SNode result = visit(ctx.bracketSpecifier());
                if (result == null) {
                    result = chainedNode;
                    chainedNode = null;
                }
                return result;
            }

            @Override
            public SNode visitBracketedExpression(SearchParser.BracketedExpressionContext ctx)
            {
                SNode chainAfterExpression = visit(ctx.bracketSpecifier());
                SNode expression = createSequenceIfChained(visit(ctx.expression()));
                chainedNode = chainAfterExpression;
                return createSequenceIfChained(expression);
            }

            @Override
            public SNode visitBracketFlatten(SearchParser.BracketFlattenContext ctx)
            {
                return createProjectionIfChained(new SFlattenArray());
            }

            @Override
            public SNode visitBracketIndex(SearchParser.BracketIndexContext ctx)
            {
                int index = Integer.parseInt(ctx.SIGNED_INT().getText());
                chainedNode = createSequenceIfChained(new SIndex(index));
                return null;
            }

            @Override
            public SNode visitBracketSlice(SearchParser.BracketSliceContext ctx)
            {
                OptionalInt start = OptionalInt.empty();
                OptionalInt stop = OptionalInt.empty();
                OptionalInt step = OptionalInt.empty();
                SearchParser.SliceContext sliceCtx = ctx.slice();
                if (sliceCtx.start != null) {
                    start = OptionalInt.of(Integer.parseInt(sliceCtx.start.getText()));
                }
                if (sliceCtx.stop != null) {
                    stop = OptionalInt.of(Integer.parseInt(sliceCtx.stop.getText()));
                }
                if (sliceCtx.step != null) {
                    step = OptionalInt.of(Integer.parseInt(sliceCtx.step.getText()));
                    if (step.getAsInt() == 0) {
                        throw new IllegalArgumentException();
                    }
                }
                chainedNode = createProjectionIfChained(new SSlice(start, stop, step));
                return null;
            }

            @Override
            public SNode visitBracketStar(SearchParser.BracketStarContext ctx)
            {
                SNode projection = (chainedNode == null) ? new SCurrent() : chainedNode;
                chainedNode = new SProject(projection);
                return null;
            }

            @Override
            public SNode visitChainExpression(SearchParser.ChainExpressionContext ctx)
            {
                chainedNode = visit(ctx.chainedExpression());
                return createSequenceIfChained(visit(ctx.expression()));
            }

            @Override
            public SNode visitComparisonExpression(SearchParser.ComparisonExpressionContext ctx)
            {
                SCmp cmp = SCmp.fromString(ctx.COMPARATOR().getText());
                SNode right = nonChainingVisit(ctx.expression(1));
                SNode left = nonChainingVisit(ctx.expression(0));
                return new SComparison(cmp, left, right);
            }

            @Override
            public SNode visitCurrentNode(SearchParser.CurrentNodeContext ctx)
            {
                if (chainedNode == null) {
                    return new SCurrent();
                }
                else {
                    SNode result = chainedNode;
                    chainedNode = null;
                    return result;
                }
            }

            @Override
            public SNode visitExpressionType(SearchParser.ExpressionTypeContext ctx)
            {
                SNode expression = createSequenceIfChained(visit(ctx.expression()));
                return new SExpressionRef(expression);
            }

            @Override
            public SNode visitFunctionExpression(SearchParser.FunctionExpressionContext ctx)
            {
                String name = ctx.NAME().getText();
                int n = ctx.functionArg().size();
                List<SNode> args = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    args.add(nonChainingVisit(ctx.functionArg(i)));
                }
                return createSequenceIfChained(new SFunctionCall(name, args));
            }

            @Override
            public SNode visitIdentifier(SearchParser.IdentifierContext ctx)
            {
                return createSequenceIfChained(new SProperty(ctx.getText()));
            }

            @Override
            public SNode visitIdentifierExpression(SearchParser.IdentifierExpressionContext ctx)
            {
                return visit(ctx.identifier());
            }

            @Override
            public SNode visitLiteral(SearchParser.LiteralContext ctx)
            {
                visit(ctx.jsonValue());
                // FIXME: unescape
                String string = ctx.jsonValue().getText();
                return new SJsonLiteral(string);
            }

            @Override
            public SNode visitMultiSelectHash(SearchParser.MultiSelectHashContext ctx)
            {
                int n = ctx.keyvalExpr().size();
                ImmutableMap.Builder<String, SNode> builder = ImmutableMap.builder();
                for (int i = 0; i < n; i++) {
                    SearchParser.KeyvalExprContext kvCtx = ctx.keyvalExpr(i);
                    // FIXME: unquote?
                    String key = kvCtx.identifier().getText();
                    SNode value = nonChainingVisit(kvCtx.expression());
                    builder.put(key, value);
                }
                return createSequenceIfChained(new SCreateObject(builder.build()));
            }

            @Override
            public SNode visitMultiSelectList(SearchParser.MultiSelectListContext ctx)
            {
                int n = ctx.expression().size();
                ImmutableList.Builder<SNode> builder = ImmutableList.builder();
                for (int i = 0; i < n; i++) {
                    builder.add(nonChainingVisit(ctx.expression(i)));
                }
                return createSequenceIfChained(new SCreateArray(builder.build()));
            }

            @Override
            public SNode visitNotExpression(SearchParser.NotExpressionContext ctx)
            {
                return new SNegate(visit(ctx.expression()));
            }

            @Override
            public SNode visitOrExpression(SearchParser.OrExpressionContext ctx)
            {
                SNode left = nonChainingVisit(ctx.expression(0));
                SNode right = nonChainingVisit(ctx.expression(1));
                return createSequenceIfChained(new SOr(left, right));
            }

            @Override
            public SNode visitParenExpression(SearchParser.ParenExpressionContext ctx)
            {
                return createSequenceIfChained(nonChainingVisit(ctx.expression()));
            }

            @Override
            public SNode visitPipeExpression(SearchParser.PipeExpressionContext ctx)
            {
                SNode right = visit(ctx.expression(1));
                SNode left = visit(ctx.expression(0));
                return new SSequence(ImmutableList.of(left, right));
            }

            @Override
            public SNode visitRawStringExpression(SearchParser.RawStringExpressionContext ctx)
            {
                // FIXME: shared escaping with tree (core.util.StringEscaping) - really, in tok.util for java/sql?
                return new SString(ctx.RAW_STRING().getText());
            }

            @Override
            public SNode visitSelect(SearchParser.SelectContext ctx)
            {
                chainedNode = createProjectionIfChained(new SSelection(nonChainingVisit(ctx.expression())));
                return null;
            }

            @Override
            public SNode visitWildcard(SearchParser.WildcardContext ctx)
            {
                return createProjectionIfChained(new SFlattenObject());
            }

            @Override
            public SNode visitWildcardExpression(SearchParser.WildcardExpressionContext ctx)
            {
                return visit(ctx.wildcard());
            }
        });
    }
}
